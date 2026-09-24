package com.mentorai.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties="mentorai.demo.enabled=true") @AutoConfigureMockMvc @ActiveProfiles("test")
class DemoIntegrationTest {
    @Autowired MockMvc mvc; @Autowired ObjectMapper json;
    @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;
    @Test void expiredDemoAndManualCheckInCannotBeOverwritten() throws Exception {
        String token=register();var seeded=start(token);var run=seeded.path("run");
        var body=Map.of("expectedRoadmapRevision",run.path("roadmapRevision").asLong(),"expectedPlanRevision",run.path("planRevision").asLong());
        jdbc.update("UPDATE demo_runs SET week_start=? WHERE id=?",java.sql.Date.valueOf(java.time.LocalDate.parse(run.path("weekStart").asText()).minusWeeks(1)),UUID.fromString(run.path("id").asText()));
        call("POST","/api/demo/exam",token,body,409);
        call("GET","/api/check-ins/current",token,null,404);
        String manual=register();start(manual);var roadmap=call("GET","/api/roadmaps/current",manual,null,200);var plan=call("GET","/api/weekly-plan/current",manual,null,200);
        List<Map<String,String>> outcomes=new ArrayList<>();for(var task:plan.path("tasks"))outcomes.add(Map.of("taskId",task.path("taskId").asText(),"outcome","MISSED"));
        var checkIn=call("POST","/api/check-ins",manual,Map.of("planId",plan.path("id").asText(),"expectedRoadmapRevision",roadmap.path("revision").asLong(),"expectedPlanRevision",plan.path("revision").asLong(),"actualHours",0,"availableHoursNextWeek",4,"energyOrCapacityBand","LOW","blockers",List.of("NO_TIME"),"tasks",outcomes),201);
        call("POST","/api/demo/exam",manual,Map.of("expectedRoadmapRevision",roadmap.path("revision").asLong(),"expectedPlanRevision",plan.path("revision").asLong()),409);
        assertThat(call("GET","/api/check-ins/current",manual,null,200)).isEqualTo(checkIn);
        assertThat(call("GET","/api/demo",manual,null,200).path("run").hasNonNull("examCheckInId")).isFalse();
    }
    @Test void seedsOnlyOwnedAccountAndRetriesKeepDeterministicState() throws Exception {
        call("GET","/api/demo",null,null,401);String token=register();var first=start(token);
        assertThat(first.path("run").path("scenarioVersion").asText()).isEqualTo("hackathon-demo-v1");
        assertThat(start(token)).isEqualTo(first);
        var profile=call("GET","/api/profile",token,null,200);assertThat(profile.path("degree").asText()).isEqualTo("BCA");
        assertThat(profile.path("timeAvailablePerWeek").asInt()).isEqualTo(8);assertThat(profile.path("skills").size()).isEqualTo(3);
        var roadmap=call("GET","/api/roadmaps/current",token,null,200);
        assertThat(task(roadmap,"Git").path("state").asText()).isEqualTo("COMPLETED");
        assertThat(call("GET","/api/demo",register(),null,200).hasNonNull("run")).isFalse();
        assertThat(call("GET","/api/profile",token,null,200)).isEqualTo(profile);
    }
    @Test void personalProfileAndExistingHistoryCannotBeOverwritten() throws Exception {
        String token=register();call("PUT","/api/profile",token,Map.of("degree","Personal degree"),200);
        var before=call("GET","/api/profile",token,null,200);
        call("POST","/api/demo/start",token,Map.of("confirmSynthetic",true),409);
        assertThat(call("GET","/api/profile",token,null,200)).isEqualTo(before);
        String other=register();call("POST","/api/mentor/conversations",other,Map.of("careerId","20000000-0000-0000-0000-000000000001"),201);
        call("POST","/api/demo/start",other,Map.of("confirmSynthetic",true),409);
        call("POST","/api/demo/start",register(),Map.of("confirmSynthetic",false),400);
    }
    @Test void examCreatesReviewableMaintenanceProposalAndPreservesCompletedWork() throws Exception {
        String token=register();var seeded=start(token);var before=call("GET","/api/roadmaps/current",token,null,200);
        var profile=call("GET","/api/profile",token,null,200);
        var plan=call("GET","/api/weekly-plan/current",token,null,200);
        var body=Map.of("expectedRoadmapRevision",before.path("revision").asLong(),"expectedPlanRevision",plan.path("revision").asLong());
        var result=call("POST","/api/demo/exam",token,body,200);
        assertThat(result.path("run").path("roadmapId")).isEqualTo(seeded.path("run").path("roadmapId"));
        var checkIn=result.path("checkIn");assertThat(checkIn.path("actualHours").asInt()).isEqualTo(2);
        assertThat(checkIn.path("adaptation").path("proposed").path("mode").asText()).isEqualTo("MAINTENANCE");
        assertThat(checkIn.path("adaptation").path("status").asText()).isEqualTo("PENDING");
        assertThat(call("POST","/api/demo/exam",token,body,200)).isEqualTo(result);
        assertThat(task(call("GET","/api/roadmaps/current",token,null,200),"Git")).isEqualTo(task(before,"Git"));
        assertThat(call("GET","/api/profile",token,null,200)).isEqualTo(profile);
    }
    @Test void sourceChangeAndMissingRunRejectExam() throws Exception {
        String token=register();call("POST","/api/demo/exam",token,Map.of("expectedRoadmapRevision",0,"expectedPlanRevision",0),404);
        start(token);var roadmap=call("GET","/api/roadmaps/current",token,null,200);
        call("PUT","/api/roadmaps/"+roadmap.path("id").asText(),token,Map.of("expectedRevision",roadmap.path("revision").asLong(),"title","Edited demo"),200);
        call("POST","/api/demo/exam",token,Map.of("expectedRoadmapRevision",roadmap.path("revision").asLong(),"expectedPlanRevision",0),409);
        call("POST","/api/roadmaps",token,Map.of("careerId",roadmap.path("careerId").asText()),201);
        call("POST","/api/demo/exam",token,Map.of("expectedRoadmapRevision",roadmap.path("revision").asLong()+1,"expectedPlanRevision",0),409);
    }
    @Test void simultaneousSeedsAndExamRetriesDoNotDuplicateWork() throws Exception {
        String token=register();
        try(var pool=Executors.newFixedThreadPool(2)){
            var gate=new CountDownLatch(1);Callable<JsonNode> seed=()->{gate.await();return start(token);};
            var a=pool.submit(seed);var b=pool.submit(seed);gate.countDown();assertThat(a.get(20,TimeUnit.SECONDS)).isEqualTo(b.get(20,TimeUnit.SECONDS));
            var roadmap=call("GET","/api/roadmaps/current",token,null,200);var plan=call("GET","/api/weekly-plan/current",token,null,200);
            var body=Map.of("expectedRoadmapRevision",roadmap.path("revision").asLong(),"expectedPlanRevision",plan.path("revision").asLong());
            var examGate=new CountDownLatch(1);Callable<JsonNode> exam=()->{examGate.await();return call("POST","/api/demo/exam",token,body,200);};
            var c=pool.submit(exam);var d=pool.submit(exam);examGate.countDown();assertThat(c.get(20,TimeUnit.SECONDS)).isEqualTo(d.get(20,TimeUnit.SECONDS));
        }
    }
    @Test void unknownRoutesAndCorsFailSafely() throws Exception {
        String token=register();var missing=call("GET","/api/unknown-demo-path",token,null,404);
        assertThat(missing.toString()).doesNotContain("Exception","stackTrace","org.springframework");
        mvc.perform(options("/api/demo").header("Origin","https://untrusted.example").header("Access-Control-Request-Method","POST")).andExpect(status().isForbidden());
        var health=call("GET","/actuator/health",null,null,200);assertThat(health.has("components")).isFalse();
        call("GET","/actuator/env",null,null,401);
    }
    JsonNode task(JsonNode roadmap,String name){for(var phase:roadmap.path("phases"))for(var task:phase.path("tasks"))if(task.path("skillName").asText().equals(name))return task;throw new AssertionError(name);}
    JsonNode start(String token)throws Exception{return call("POST","/api/demo/start",token,Map.of("confirmSynthetic",true),200);}
    String register()throws Exception{return call("POST","/api/auth/register",null,Map.of("displayName","DEMO Tester","email","demo-"+UUID.randomUUID()+"@example.com","password","demo-test-password-0923"),201).path("accessToken").asText();}
    JsonNode call(String method,String path,String token,Object body,int expected)throws Exception{
        var request=switch(method){case "POST"->post(path);case "PUT"->put(path);default->get(path);};
        if(token!=null)request.header("Authorization","Bearer "+token);if(body!=null)request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(body));
        return json.readTree(mvc.perform(request).andExpect(status().is(expected)).andReturn().getResponse().getContentAsByteArray());
    }
}
