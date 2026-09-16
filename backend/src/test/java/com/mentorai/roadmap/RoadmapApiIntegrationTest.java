package com.mentorai.roadmap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
class RoadmapApiIntegrationTest {
    private static final String CAREER = "20000000-0000-0000-0000-000000000001";
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void generatesSavedDependencyOrderAndBoundedPartialWeeklyFocus() throws Exception {
        String token = register(1);
        JsonNode roadmap = create(token);
        List<String> names = tasks(roadmap).stream().map(task -> task.path("skillName").asText()).toList();
        assertThat(names).hasSize(9).doesNotHaveDuplicates();
        assertThat(names.indexOf("Java")).isLessThan(names.indexOf("Spring Fundamentals"));
        assertThat(names.indexOf("Spring Fundamentals")).isLessThan(names.indexOf("Spring Boot"));
        assertThat(roadmap.path("nextAction").path("skillName").asText()).isEqualTo("Java");
        assertThat(roadmap.path("thisWeek").size()).isEqualTo(1);
        assertThat(roadmap.path("thisWeek").get(0).path("plannedHours").asInt()).isEqualTo(1);
        assertThat(roadmap.path("nextAction").path("estimatedHours").asInt()).isEqualTo(8);
        assertThat(read(token, roadmap.path("id").asText())).isEqualTo(roadmap);
        assertThat(read(token, "current").path("id")).isEqualTo(roadmap.path("id"));
    }

    @Test
    void skipsRecordedTargetsWithoutMutatingProfileAndPreservesEarlierGeneration() throws Exception {
        String token = register(8);
        saveProfile(token, """
                {"timeAvailablePerWeek":8,"skills":[
                {"name":"Java","category":"Language","proficiency":"INTERMEDIATE","confidence":"MEDIUM","source":"SELF_REPORTED"},
                {"name":"Spring Fundamentals","category":"Backend","proficiency":"BEGINNER","confidence":"MEDIUM","source":"SELF_REPORTED"}]}
                """);
        JsonNode before = profile(token);
        JsonNode first = create(token);
        assertThat(task(first, "Java").path("state").asText()).isEqualTo("SKIPPED");
        assertThat(task(first, "Java").path("satisfiedAtGeneration").asBoolean()).isTrue();
        assertThat(task(first, "Spring Fundamentals").path("state").asText()).isEqualTo("SKIPPED");
        assertThat(task(first, "Spring Boot").path("ready").asBoolean()).isTrue();
        assertThat(profile(token)).isEqualTo(before);
        JsonNode second = create(token);
        assertThat(second.path("id")).isNotEqualTo(first.path("id"));
        assertThat(second.path("previousRoadmapId")).isEqualTo(first.path("id"));
        assertThat(read(token, "current").path("id")).isEqualTo(second.path("id"));
        assertThat(read(token, first.path("id").asText())).isEqualTo(first);
    }

    @Test
    void enforcesOwnershipOnReadAndUpdate() throws Exception {
        String owner = register(8);
        String other = register(8);
        JsonNode roadmap = create(owner);
        String id = roadmap.path("id").asText();
        mvc.perform(get("/api/roadmaps/" + id).header("Authorization", other)).andExpect(status().isNotFound());
        mvc.perform(put("/api/roadmaps/" + id).header("Authorization", other).contentType(MediaType.APPLICATION_JSON)
                .content("{\"expectedRevision\":0,\"title\":\"Not mine\",\"tasks\":[]}"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/roadmaps/current").header("Authorization", other)).andExpect(status().isNotFound());
    }

    @Test
    void allowsValidTransitionsButSkippedUnknownPrerequisitesRemainBlocked() throws Exception {
        String token = register(8);
        JsonNode roadmap = create(token);
        roadmap = update(token, roadmap, List.of(edit(task(roadmap, "Java"), "SKIPPED")), 200);
        update(token, roadmap, List.of(edit(task(roadmap, "Spring Fundamentals"), "IN_PROGRESS")), 409);
        roadmap = update(token, roadmap, List.of(edit(task(roadmap, "Java"), "NOT_STARTED")), 200);
        roadmap = update(token, roadmap, List.of(edit(task(roadmap, "Java"), "COMPLETED")), 200);
        update(token, roadmap, List.of(edit(task(roadmap, "Java"), "NOT_STARTED")), 409);
        roadmap = update(token, roadmap, List.of(edit(task(roadmap, "Spring Fundamentals"), "IN_PROGRESS")), 200);
        assertThat(task(roadmap, "Spring Fundamentals").path("state").asText()).isEqualTo("IN_PROGRESS");
        roadmap = update(token, roadmap, List.of(edit(task(roadmap, "Java"), "NEEDS_REVIEW")), 200);
        update(token, roadmap, List.of(edit(task(roadmap, "Spring Fundamentals"), "COMPLETED")), 409);
    }

    @Test
    void batchChangesAreAtomicAndIndependentOfRequestOrder() throws Exception {
        String token = register(8);
        JsonNode roadmap = create(token);
        var rename = new java.util.HashMap<>(edit(task(roadmap, "Java"), "NOT_STARTED"));
        rename.put("title", "This must roll back");
        update(token, roadmap, List.of(rename, edit(task(roadmap, "Spring Fundamentals"), "COMPLETED")), 409);
        assertThat(read(token, roadmap.path("id").asText())).isEqualTo(roadmap);
        roadmap = update(token, roadmap, List.of(edit(task(roadmap, "Spring Fundamentals"), "COMPLETED"),
                edit(task(roadmap, "Java"), "COMPLETED")), 200);
        assertThat(task(roadmap, "Spring Boot").path("ready").asBoolean()).isTrue();
    }

    @Test
    void rejectsStaleRevisionAndInvalidTaskEdits() throws Exception {
        String token = register(8);
        JsonNode original = create(token);
        JsonNode updated = update(token, original, List.of(edit(task(original, "Java"), "IN_PROGRESS")), 200);
        assertThat(updated.path("revision").asLong()).isGreaterThan(original.path("revision").asLong());
        update(token, original, List.of(edit(task(original, "Java"), "SKIPPED")), 409);
        var duplicate = edit(task(updated, "Java"), "IN_PROGRESS");
        update(token, updated, List.of(duplicate, duplicate), 400);
        var foreign = new java.util.HashMap<>(duplicate);
        foreign.put("id", UUID.randomUUID().toString());
        update(token, updated, List.of(foreign), 400);
        var invalidEffort = new java.util.HashMap<>(duplicate);
        invalidEffort.put("estimatedHours", 0);
        update(token, updated, List.of(invalidEffort), 400);
        assertThat(read(token, updated.path("id").asText())).isEqualTo(updated);
    }

    @Test
    void validatesInputRequiresAvailabilityAndAuthenticatesAllEndpoints() throws Exception {
        String token = register(null);
        mvc.perform(post("/api/roadmaps").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"careerId\":\"" + CAREER + "\"}")).andExpect(status().isUnprocessableEntity());
        mvc.perform(post("/api/roadmaps").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"careerId\":\"invalid\"}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/roadmaps").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/roadmaps").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"careerId\":\"" + UUID.randomUUID() + "\"}")).andExpect(status().isNotFound());
        mvc.perform(get("/api/roadmaps/current")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/roadmaps/" + UUID.randomUUID())).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/roadmaps").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/api/roadmaps/" + UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    private JsonNode create(String token) throws Exception {
        return mapper.readTree(mvc.perform(post("/api/roadmaps").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"careerId\":\"" + CAREER + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
    }
    private JsonNode read(String token, String id) throws Exception {
        return mapper.readTree(mvc.perform(get("/api/roadmaps/" + id).header("Authorization", token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }
    private JsonNode update(String token, JsonNode roadmap, List<Map<String, Object>> edits, int expectedStatus) throws Exception {
        String body = mapper.writeValueAsString(Map.of("expectedRevision", roadmap.path("revision").asLong(), "tasks", edits));
        String response = mvc.perform(put("/api/roadmaps/" + roadmap.path("id").asText())
                .header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().is(expectedStatus)).andReturn().getResponse().getContentAsString();
        return mapper.readTree(response);
    }
    private Map<String, Object> edit(JsonNode task, String state) {
        return Map.of("id", task.path("id").asText(), "title", task.path("title").asText(),
                "estimatedHours", Math.max(1, task.path("estimatedHours").asInt()), "state", state);
    }
    private List<JsonNode> tasks(JsonNode roadmap) {
        List<JsonNode> result = new ArrayList<>();
        roadmap.path("phases").forEach(phase -> phase.path("tasks").forEach(result::add));
        return result;
    }
    private JsonNode task(JsonNode roadmap, String name) {
        return tasks(roadmap).stream().filter(task -> name.equals(task.path("skillName").asText())).findFirst().orElseThrow();
    }
    private String register(Integer hours) throws Exception {
        String body = """
                {"displayName":"DEMO Roadmap Student","email":"%s@example.com","password":"demo-roadmap-test-password"}
                """.formatted(UUID.randomUUID());
        String token = "Bearer " + mapper.readTree(mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString()).path("accessToken").asText();
        if (hours != null) saveProfile(token, "{\"timeAvailablePerWeek\":" + hours + "}");
        return token;
    }
    private void saveProfile(String token, String body) throws Exception {
        mvc.perform(put("/api/profile").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content(body)).andExpect(status().isOk());
    }
    private JsonNode profile(String token) throws Exception {
        return mapper.readTree(mvc.perform(get("/api/profile").header("Authorization", token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }
}
