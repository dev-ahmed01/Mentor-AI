package com.mentorai.mentor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class MentorApiIntegrationTest {
    static final String CAREER="20000000-0000-0000-0000-000000000001";
    @Autowired MockMvc mvc; @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @BeforeEach void clearMarketFixtures(){jdbc.update("delete from market_decisions");jdbc.update("delete from market_snapshot_observations");jdbc.update("delete from market_snapshots");jdbc.update("delete from market_observations");}
    @Test void mentorRequiresAuthAndListsOnlyOwnedConversations() throws Exception {
        call("GET","/api/mentor/conversations",null,null,401);
        String token=register();var conversation=create(token);
        assertThat(call("GET","/api/mentor/conversations",token,null,200).toString()).contains(conversation.path("id").asText());
        String other=register();assertThat(call("GET","/api/mentor/conversations",other,null,200)).isEmpty();
        call("GET","/api/mentor/conversations/"+conversation.path("id").asText(),other,null,404);
    }
    @Test void disabledProviderPersistsExplicitUnavailableTurnAndRetriedRequestIsIdempotent() throws Exception {
        String token=register();var before=call("GET","/api/profile",token,null,200);var conversation=create(token);
        String path="/api/mentor/conversations/"+conversation.path("id").asText()+"/messages";
        var body=Map.of("requestId",UUID.randomUUID(),"expectedRevision",0,"question","Should I learn Kubernetes now?");
        var turn=call("POST",path,token,body,201);
        assertThat(turn.path("status").asText()).isEqualTo("UNAVAILABLE");
        assertThat(turn.path("answer").asText()).contains("AI mentor is currently unavailable");
        assertThat(turn.path("marketNotice").asText()).isEqualTo("Insufficient market evidence available.");
        assertThat(call("POST",path,token,body,201)).isEqualTo(turn);
        assertThat(call("GET","/api/profile",token,null,200)).isEqualTo(before);
        call("POST",path,token,Map.of("requestId",UUID.randomUUID(),"expectedRevision",0,"question","A stale request"),409);
        var history=call("GET",path.replace("/messages",""),token,null,200);
        assertThat(history.path("turns").size()).isEqualTo(1);
        assertThat(history.path("conversation").path("revision").asLong()).isEqualTo(1);
    }
    @Test void validatesConversationAndMessageInputsBeforeProviderUse() throws Exception {
        String token=register();
        call("POST","/api/mentor/conversations",token,Map.of("careerId",UUID.randomUUID()),404);
        call("POST","/api/mentor/conversations",token,Map.of("careerId",CAREER,"jobAnalysisId",UUID.randomUUID()),404);
        var conversation=create(token);String path="/api/mentor/conversations/"+conversation.path("id").asText()+"/messages";
        call("POST",path,token,Map.of("requestId",UUID.randomUUID(),"expectedRevision",0,"question"," "),400);
        call("POST",path,token,Map.of("requestId",UUID.randomUUID(),"expectedRevision",0,"question","x".repeat(2001)),400);
        call("POST",path,register(),Map.of("requestId",UUID.randomUUID(),"expectedRevision",0,"question","Private?"),404);
        call("GET","/api/mentor/conversations/"+conversation.path("id").asText()+"?page=-1",token,null,400);
    }
    JsonNode create(String token) throws Exception {return call("POST","/api/mentor/conversations",token,Map.of("careerId",CAREER),201);}
    String register() throws Exception {return call("POST","/api/auth/register",null,Map.of("displayName","DEMO Mentor Test","email","mentor-"+UUID.randomUUID()+"@example.com","password","mentor-test-password-0923"),201).path("accessToken").asText();}
    JsonNode call(String method,String path,String token,Object body,int expected) throws Exception {
        var request=switch(method){case "POST"->post(path);case "PUT"->put(path);default->get(path);};
        if(token!=null)request.header("Authorization","Bearer "+token);
        if(body!=null)request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(body));
        return json.readTree(mvc.perform(request).andExpect(status().is(expected)).andReturn().getResponse().getContentAsByteArray());
    }
}
