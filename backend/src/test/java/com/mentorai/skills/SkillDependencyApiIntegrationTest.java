package com.mentorai.skills;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentorai.skills.repository.SkillRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SkillDependencyApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired SkillRepository skills;
    @Autowired JdbcTemplate jdbc;

    @Test
    void returnsDirectAndTransitivePrerequisites() throws Exception {
        String token = register();
        UUID target = skillId("spring boot");
        mvc.perform(get("/api/skills/{id}/dependencies", target).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].prerequisiteName", is("Spring Fundamentals")));
        mvc.perform(get("/api/skills/{id}/prerequisites", target).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligible", is(false)))
                .andExpect(jsonPath("$.prerequisites", hasSize(2)))
                .andExpect(jsonPath("$.prerequisites[*].name", hasItem("Java")))
                .andExpect(jsonPath("$.dataLabel", is("DEMO DATA")));
    }

    @Test
    void distinguishesAwarenessFromUsableFoundationsAndIsolatesStudents() throws Exception {
        String first = register();
        String second = register();
        UUID target = skillId("spring boot");
        saveSkills(first, "AWARENESS");
        mvc.perform(get("/api/skills/{id}/prerequisites", target).header("Authorization", first))
                .andExpect(status().isOk()).andExpect(jsonPath("$.eligible", is(false)));
        saveSkills(first, "BEGINNER");
        mvc.perform(get("/api/skills/{id}/prerequisites", target).header("Authorization", first))
                .andExpect(status().isOk()).andExpect(jsonPath("$.eligible", is(true)))
                .andExpect(jsonPath("$.prerequisites[0].satisfied", is(true)))
                .andExpect(jsonPath("$.prerequisites[1].satisfied", is(true)));
        mvc.perform(get("/api/skills/{id}/prerequisites", target).header("Authorization", second))
                .andExpect(status().isOk()).andExpect(jsonPath("$.eligible", is(false)));
    }

    @Test
    void noRecordedPrerequisitesIsExplicitAndDoesNotMutateProfile() throws Exception {
        String token = register();
        JsonNode before = profile(token);
        mvc.perform(get("/api/skills/{id}/prerequisites", skillId("java"))
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligible", is(true)))
                .andExpect(jsonPath("$.coverage", is("NO_RECORDED_PREREQUISITES")))
                .andExpect(jsonPath("$.prerequisites", hasSize(0)));
        assertThat(profile(token)).isEqualTo(before);
    }

    @Test
    void supportsCareerBatchAndRejectsInvalidResources() throws Exception {
        String token = register();
        mvc.perform(get("/api/skills/prerequisites")
                        .param("careerId", "20000000-0000-0000-0000-000000000001")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("Spring Boot")));
        mvc.perform(get("/api/skills/{id}/prerequisites", UUID.randomUUID())
                        .header("Authorization", token)).andExpect(status().isNotFound());
        mvc.perform(get("/api/skills/prerequisites").param("careerId", UUID.randomUUID().toString())
                        .header("Authorization", token)).andExpect(status().isNotFound());
        mvc.perform(get("/api/skills/not-a-uuid/prerequisites").header("Authorization", token))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/skills/prerequisites").header("Authorization", token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void allReadEndpointsRequireAuthentication() throws Exception {
        mvc.perform(get("/api/skills/{id}/dependencies", skillId("java")))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/skills/{id}/prerequisites", skillId("java")))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/skills/prerequisites")
                        .param("careerId", "20000000-0000-0000-0000-000000000001"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void databasePreventsDuplicateAndSelfEdges() {
        UUID target = skillId("spring boot");
        UUID prerequisite = skillId("spring fundamentals");
        String insert = "INSERT INTO skill_dependencies (id, skill_id, prerequisite_skill_id, importance) VALUES (?, ?, ?, ?)";
        assertThatThrownBy(() -> jdbc.update(insert, UUID.randomUUID(), target, prerequisite, 5))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update(insert, UUID.randomUUID(), target, target, 5))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update(insert, UUID.randomUUID(), skillId("java"), skillId("sql"), 0))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private UUID skillId(String name) {
        return skills.findByNormalizedName(name).orElseThrow().getId();
    }

    private String register() throws Exception {
        String result = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"displayName":"Dependency Student","email":"%s@example.com","password":"demo-dependency-test-password"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return "Bearer " + mapper.readTree(result).get("accessToken").asText();
    }

    private void saveSkills(String token, String proficiency) throws Exception {
        mvc.perform(put("/api/profile").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skills":[
                                {"name":"Java","category":"Language","proficiency":"%s","confidence":"MEDIUM","source":"SELF_REPORTED"},
                                {"name":"Spring Fundamentals","category":"Backend","proficiency":"BEGINNER","confidence":"MEDIUM","source":"SELF_REPORTED"}
                                ]}
                                """.formatted(proficiency)))
                .andExpect(status().isOk());
    }

    private JsonNode profile(String token) throws Exception {
        return mapper.readTree(mvc.perform(get("/api/profile").header("Authorization", token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }
}
