package com.mentorai.demo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class DemoDisabledIntegrationTest {
    @Autowired MockMvc mvc; @Autowired ObjectMapper json;
    @Test void disabledByDefaultAndNoMutationIsAvailable()throws Exception{
        var registration=mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(Map.of("displayName","Demo Disabled","email","demo-disabled-"+UUID.randomUUID()+"@example.com","password","demo-disabled-password")))).andExpect(status().isCreated()).andReturn();
        String token="Bearer "+json.readTree(registration.getResponse().getContentAsByteArray()).path("accessToken").asText();
        mvc.perform(get("/api/demo").header("Authorization",token)).andExpect(status().isOk()).andExpect(jsonPath("$.enabled").value(false));
        mvc.perform(post("/api/demo/start").header("Authorization",token).contentType(MediaType.APPLICATION_JSON).content("{\"confirmSynthetic\":true}")).andExpect(status().isNotFound());
    }
}
