package com.mentorai.decision;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LearningDecisionApiIntegrationTest {
    private static final String ENDPOINT = "/api/decisions/learning-priorities";
    private static final String CAREER = "20000000-0000-0000-0000-000000000001";
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void blocksMissingPrerequisitesAndIncludesTheFoundationToLearnFirst() throws Exception {
        String token = register();
        save(token, "{\"timeAvailablePerWeek\":8}");
        JsonNode result = decisions(token);
        assertThat(skill(result, "Spring Boot").path("priority").asText()).isEqualTo("NOT_YET");
        assertThat(skill(result, "Spring Boot").path("reasonCodes").toString()).contains("PREREQUISITES_MISSING");
        assertThat(skill(result, "Java").path("priority").asText()).isEqualTo("LEARN_NOW");
        assertThat(skill(result, "Spring Fundamentals").path("priority").asText()).isEqualTo("NOT_YET");
        assertThat(result.path("decisions").size()).isEqualTo(9);
        assertThat(result.path("marketEvidenceStatus").asText()).isEqualTo("UNAVAILABLE");
        assertThat(result.path("marketWeight").asInt()).isZero();
        assertThat(result.has("marketScore")).isFalse();
    }

    @Test
    void unlocksRequiredSkillAndDoesNotRepeatCompletedLearning() throws Exception {
        String token = register();
        save(token, profile(12, "INTERMEDIATE", "BEGINNER"));
        JsonNode result = decisions(token);
        assertThat(skill(result, "Spring Boot").path("priority").asText()).isEqualTo("LEARN_NOW");
        for (String name : new String[]{"Java", "Spring Fundamentals"}) {
            assertThat(skill(result, name).path("priority").asText()).isEqualTo("NOT_YET");
            assertThat(skill(result, name).path("reasonCodes").toString()).contains("ALREADY_PROFICIENT");
            assertThat(skill(result, name).path("deterministicScore").asInt()).isZero();
        }
        assertThat(skill(result, "Spring Boot").path("deterministicScore").asInt())
                .isGreaterThan(skill(result, "Docker").path("deterministicScore").asInt());
    }

    @Test
    void limitsImmediateWorkAndDoesNotAssumeMissingAvailability() throws Exception {
        String token = register();
        assertThat(count(decisions(token), "LEARN_NOW")).isZero();
        save(token, "{\"timeAvailablePerWeek\":2}");
        JsonNode low = decisions(token);
        assertThat(count(low, "LEARN_NOW")).isEqualTo(1);
        assertThat(low.toString()).contains("TIME_BUDGET_CONSTRAINT");
        save(token, "{\"timeAvailablePerWeek\":12}");
        assertThat(count(decisions(token), "LEARN_NOW")).isEqualTo(3);
    }

    @Test
    void isReproducibleReadOnlyAndIsolatedByAuthenticatedStudent() throws Exception {
        String first = register();
        String second = register();
        save(first, profile(8, "BEGINNER", "BEGINNER"));
        JsonNode before = readProfile(first);
        JsonNode result = decisions(first);
        assertThat(decisions(first)).isEqualTo(result);
        assertThat(readProfile(first)).isEqualTo(before);
        assertThat(skill(result, "Spring Boot").path("prerequisiteReadiness").path("eligible").asBoolean()).isTrue();
        assertThat(skill(decisions(second), "Spring Boot").path("priority").asText()).isEqualTo("NOT_YET");
        var ids = new java.util.HashSet<String>();
        result.path("decisions").forEach(item -> assertThat(ids.add(item.path("skillId").asText())).isTrue());
    }

    @Test
    void requiresAuthenticationAndValidActiveCareer() throws Exception {
        mvc.perform(get(ENDPOINT).param("careerId", CAREER)).andExpect(status().isUnauthorized());
        String token = register();
        mvc.perform(get(ENDPOINT).header("Authorization", token)).andExpect(status().isBadRequest());
        mvc.perform(get(ENDPOINT).param("careerId", "invalid").header("Authorization", token))
                .andExpect(status().isBadRequest());
        mvc.perform(get(ENDPOINT).param("careerId", UUID.randomUUID().toString()).header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    private JsonNode decisions(String token) throws Exception {
        return mapper.readTree(mvc.perform(get(ENDPOINT).param("careerId", CAREER).header("Authorization", token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }
    private JsonNode skill(JsonNode response, String name) {
        for (JsonNode item : response.path("decisions")) if (name.equals(item.path("name").asText())) return item;
        throw new AssertionError("Missing decision for " + name);
    }
    private long count(JsonNode response, String priority) {
        return java.util.stream.StreamSupport.stream(response.path("decisions").spliterator(), false)
                .filter(item -> priority.equals(item.path("priority").asText())).count();
    }
    private String register() throws Exception {
        String body = """
                {"displayName":"DEMO Decision Student","email":"%s@example.com","password":"demo-decision-test-password"}
                """.formatted(UUID.randomUUID());
        return "Bearer " + mapper.readTree(mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString()).path("accessToken").asText();
    }
    private void save(String token, String body) throws Exception {
        mvc.perform(put("/api/profile").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content(body)).andExpect(status().isOk());
    }
    private JsonNode readProfile(String token) throws Exception {
        return mapper.readTree(mvc.perform(get("/api/profile").header("Authorization", token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }
    private String profile(int hours, String java, String spring) {
        return """
                {"timeAvailablePerWeek":%d,"skills":[
                {"name":"Java","category":"Language","proficiency":"%s","confidence":"MEDIUM","source":"SELF_REPORTED"},
                {"name":"Spring Fundamentals","category":"Backend","proficiency":"%s","confidence":"MEDIUM","source":"SELF_REPORTED"}]}
                """.formatted(hours, java, spring);
    }
}
