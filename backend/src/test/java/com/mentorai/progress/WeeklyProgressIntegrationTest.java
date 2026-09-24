package com.mentorai.progress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(WeeklyProgressIntegrationTest.TimeConfig.class)
class WeeklyProgressIntegrationTest {
    private static final String CAREER = "20000000-0000-0000-0000-000000000001";
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired MutableClock clock;

    @BeforeEach void resetClock() { clock.set(Instant.parse("2026-09-16T12:00:00Z")); }

    @Test
    void createsPersistentCurrentSnapshotAndRejectsDuplicateWithoutChangingProfile() throws Exception {
        String token = register(8);
        JsonNode profile = getJson("/api/profile", token, 200);
        JsonNode roadmap = createRoadmap(token);
        JsonNode plan = postJson("/api/weekly-plan", token, Map.of("roadmapId", roadmap.path("id").asText()), 201);
        assertThat(plan.path("weekStart").asText()).isEqualTo("2026-09-14");
        assertThat(plan.path("capacityHours").asInt()).isEqualTo(8);
        assertThat(plan.path("plannedHours").asInt()).isEqualTo(8);
        assertThat(plan.path("roadmapRevision").asLong()).isEqualTo(roadmap.path("revision").asLong());
        assertThat(plan.path("tasks")).hasSize(1);
        assertThat(getJson("/api/weekly-plan/current", token, 200).path("id")).isEqualTo(plan.path("id"));
        postJson("/api/weekly-plan", token, Map.of("roadmapId", roadmap.path("id").asText()), 409);
        assertThat(getJson("/api/profile", token, 200)).isEqualTo(profile);
    }

    @Test
    void submitsPartialAndMissedOutcomesAtomicallyAndCreatesNextPlan() throws Exception {
        String token = register(16);
        JsonNode roadmap = createRoadmap(token);
        JsonNode plan = postJson("/api/weekly-plan", token, Map.of("roadmapId", roadmap.path("id").asText()), 201);
        List<Map<String, Object>> outcomes = new ArrayList<>();
        outcomes.add(Map.of("taskId", plan.path("tasks").get(0).path("taskId").asText(), "outcome", "PARTIAL"));
        outcomes.add(Map.of("taskId", plan.path("tasks").get(1).path("taskId").asText(), "outcome", "MISSED"));
        JsonNode checkIn = postJson("/api/check-ins", token, checkIn(plan, 4, 1, outcomes), 201);
        assertThat(checkIn.path("actualHours").asInt()).isEqualTo(4);
        assertThat(checkIn.path("nextPlan").path("weekStart").asText()).isEqualTo("2026-09-21");
        assertThat(checkIn.path("nextPlan").path("capacityHours").asInt()).isEqualTo(1);
        JsonNode updated = getJson("/api/roadmaps/" + roadmap.path("id").asText(), token, 200);
        assertThat(findTask(updated, outcomes.get(0).get("taskId").toString()).path("state").asText()).isEqualTo("IN_PROGRESS");
        assertThat(findTask(updated, outcomes.get(1).get("taskId").toString()).path("state").asText()).isEqualTo("NOT_STARTED");
    }

    @Test
    void validatesHoursRatingsDatesOutcomesAndRollsBackStaleSubmission() throws Exception {
        String token = register(8);
        JsonNode roadmap = createRoadmap(token);
        JsonNode plan = postJson("/api/weekly-plan", token, Map.of("roadmapId", roadmap.path("id").asText()), 201);
        List<Map<String, Object>> valid = outcomes(plan, "MISSED");
        Map<String, Object> bad = new java.util.LinkedHashMap<>(checkIn(plan, -1, 8, valid));
        postJson("/api/check-ins", token, bad, 400);
        bad = new java.util.LinkedHashMap<>(checkIn(plan, 1, 169, valid));
        postJson("/api/check-ins", token, bad, 400);
        bad = new java.util.LinkedHashMap<>(checkIn(plan, 1, 8, valid)); bad.put("difficultyRating", 6);
        postJson("/api/check-ins", token, bad, 400);
        bad = new java.util.LinkedHashMap<>(checkIn(plan, 1, 8, valid)); bad.put("confidenceRating", 2.5);
        postJson("/api/check-ins", token, bad, 400);
        bad = new java.util.LinkedHashMap<>(checkIn(plan, 1, 8, valid)); bad.put("constraint", Map.of("type", "EXAMS", "startDate", "2026-10-02", "endDate", "2026-10-01"));
        postJson("/api/check-ins", token, bad, 400);
        List<Map<String, Object>> duplicate = new ArrayList<>(valid); duplicate.add(valid.getFirst());
        postJson("/api/check-ins", token, checkIn(plan, 1, 8, duplicate), 400);
        List<Map<String, Object>> foreign = List.of(Map.of("taskId", UUID.randomUUID().toString(), "outcome", "MISSED"));
        postJson("/api/check-ins", token, checkIn(plan, 1, 8, foreign), 400);
        bad = new java.util.LinkedHashMap<>(checkIn(plan, 1, 8, valid)); bad.put("expectedRoadmapRevision", 99);
        postJson("/api/check-ins", token, bad, 409);
        assertThat(getJson("/api/check-ins/current", token, 404).path("code").asText()).isEqualTo("RESOURCE_NOT_FOUND");
        clock.set(Instant.parse("2026-09-23T12:00:00Z"));
        getJson("/api/weekly-plan/current?weekStart=2026-09-21", token, 404);
        assertThat(getJson("/api/roadmaps/" + roadmap.path("id").asText(), token, 200).path("revision"))
                .isEqualTo(roadmap.path("revision"));

        bad = new java.util.LinkedHashMap<>(checkIn(plan, 1, 8, valid)); bad.put("actualHours", 1.5);
        postJson("/api/check-ins", token, bad, 400);
        bad = new java.util.LinkedHashMap<>(checkIn(plan, 1, 8, valid)); bad.put("availableHoursNextWeek", 8.25);
        postJson("/api/check-ins", token, bad, 400);
    }

    @Test
    void completedWorkIsOmittedAndUnlocksItsDependentFoundation() throws Exception {
        String token = register(16);
        JsonNode roadmap = createRoadmap(token);
        JsonNode profile = getJson("/api/profile", token, 200);
        JsonNode plan = postJson("/api/weekly-plan", token, Map.of("roadmapId", roadmap.path("id").asText()), 201);
        String completedId = plan.path("tasks").get(0).path("taskId").asText();
        JsonNode checkIn = postJson("/api/check-ins", token, checkIn(plan, 16, 16, outcomes(plan, "COMPLETED")), 201);
        assertThat(checkIn.path("nextPlan").path("tasks").findValuesAsText("taskId")).doesNotContain(completedId);
        JsonNode updated = getJson("/api/roadmaps/" + roadmap.path("id").asText(), token, 200);
        assertThat(findTask(updated, completedId).path("state").asText()).isEqualTo("COMPLETED");
        JsonNode spring = findTaskByName(updated, "Spring Fundamentals");
        assertThat(spring.path("ready").asBoolean()).isTrue();
        assertThat(checkIn.path("nextPlan").path("tasks").findValuesAsText("taskId"))
                .contains(spring.path("id").asText());
        assertThat(getJson("/api/profile", token, 200)).isEqualTo(profile);
    }

    @Test
    void deferredWorkIsExcludedForOnlyTheImmediateNextAllocation() throws Exception {
        String token = register(16);
        JsonNode roadmap = createRoadmap(token);
        JsonNode plan = postJson("/api/weekly-plan", token, Map.of("roadmapId", roadmap.path("id").asText()), 201);
        assertThat(plan.path("tasks").size()).isGreaterThanOrEqualTo(2);
        String deferredId = plan.path("tasks").get(0).path("taskId").asText();
        String missedId = plan.path("tasks").get(1).path("taskId").asText();
        List<Map<String, Object>> outcomes = new ArrayList<>();
        for (JsonNode task : plan.path("tasks")) outcomes.add(Map.of("taskId", task.path("taskId").asText(),
                "outcome", task.path("taskId").asText().equals(deferredId) ? "DEFERRED" : "MISSED"));
        JsonNode next = postJson("/api/check-ins", token, checkIn(plan, 0, 16, outcomes), 201).path("nextPlan");
        assertThat(next.path("tasks").findValuesAsText("taskId")).doesNotContain(deferredId).contains(missedId);
        assertThat(findTask(getJson("/api/roadmaps/" + roadmap.path("id").asText(), token, 200), deferredId)
                .path("state").asText()).isEqualTo("NOT_STARTED");
    }

    @Test
    void rejectsAPlanThatHasBecomeFutureRelativeToTheInjectedClock() throws Exception {
        String token = register(8);
        JsonNode roadmap = createRoadmap(token);
        JsonNode plan = postJson("/api/weekly-plan", token, Map.of("roadmapId", roadmap.path("id").asText()), 201);
        clock.set(Instant.parse("2026-09-09T12:00:00Z"));
        postJson("/api/check-ins", token, checkIn(plan, 0, 8, outcomes(plan, "MISSED")), 400);
        getJson("/api/check-ins/current?weekStart=2026-09-14", token, 400);
    }

    @Test
    void historyUsesTwentyItemPagesNewestFirst() throws Exception {
        String token = register(8);
        JsonNode roadmap = createRoadmap(token);
        JsonNode plan = postJson("/api/weekly-plan", token, Map.of("roadmapId", roadmap.path("id").asText()), 201);
        for (int week = 0; week < 20; week++) {
            JsonNode checkIn = postJson("/api/check-ins", token, checkIn(plan, 0, 8, outcomes(plan, "MISSED")), 201);
            plan = checkIn.path("nextPlan");
            clock.set(clock.instant().plus(7, java.time.temporal.ChronoUnit.DAYS));
        }
        JsonNode first = getJson("/api/check-ins/history?page=0", token, 200);
        JsonNode second = getJson("/api/check-ins/history?page=1", token, 200);
        assertThat(first.path("items")).hasSize(20);
        assertThat(first.path("hasNext").asBoolean()).isTrue();
        assertThat(second.path("items")).hasSize(1);
        assertThat(second.path("hasNext").asBoolean()).isFalse();
        assertThat(first.path("items").get(0).path("plan").path("weekStart").asText())
                .isGreaterThan(first.path("items").get(19).path("plan").path("weekStart").asText());
        getJson("/api/check-ins/history?page=1000", token, 200);
    }

    @Test
    void supportsZeroAnd168HourPlansTemporaryConstraintLateCheckInAndExistingNextPlanPreservation() throws Exception {
        String token = register(8);
        JsonNode roadmap = createRoadmap(token);
        JsonNode oldPlan = postJson("/api/weekly-plan", token, Map.of("roadmapId", roadmap.path("id").asText()), 201);
        clock.set(Instant.parse("2026-09-23T12:00:00Z"));
        JsonNode existing = postJson("/api/weekly-plan", token, Map.of("roadmapId", roadmap.path("id").asText()), 201);
        Map<String, Object> request = new java.util.LinkedHashMap<>(checkIn(oldPlan, 0, 168, outcomes(oldPlan, "DEFERRED")));
        request.put("constraint", Map.of("type", "EXAMS", "startDate", "2026-09-24", "endDate", "2026-09-30"));
        JsonNode checkIn = postJson("/api/check-ins", token, request, 201);
        assertThat(checkIn.path("nextPlan").path("id")).isEqualTo(existing.path("id"));
        assertThat(checkIn.path("explanation").asText()).containsIgnoringCase("preserved");

        String restful = register(8);
        JsonNode restfulRoadmap = createRoadmap(restful);
        JsonNode restfulPlan = postJson("/api/weekly-plan", restful, Map.of("roadmapId", restfulRoadmap.path("id").asText()), 201);
        JsonNode zero = postJson("/api/check-ins", restful, checkIn(restfulPlan, 0, 0, outcomes(restfulPlan, "MISSED")), 201);
        assertThat(zero.path("nextPlan").path("tasks")).isEmpty();
        assertThat(zero.path("nextPlan").path("plannedHours").asInt()).isZero();

        String twentyHour = register(8);
        JsonNode twentyRoadmap = createRoadmap(twentyHour);
        JsonNode twentyPlan = postJson("/api/weekly-plan", twentyHour, Map.of("roadmapId", twentyRoadmap.path("id").asText()), 201);
        JsonNode twenty = postJson("/api/check-ins", twentyHour,
                checkIn(twentyPlan, 1, 20, outcomes(twentyPlan, "MISSED")), 201);
        assertThat(twenty.path("nextPlan").path("capacityHours").asInt()).isEqualTo(20);
        assertThat(twenty.path("nextPlan").path("plannedHours").asInt()).isLessThanOrEqualTo(20);

        String maximum = register(8);
        JsonNode maximumRoadmap = createRoadmap(maximum);
        JsonNode maximumPlan = postJson("/api/weekly-plan", maximum, Map.of("roadmapId", maximumRoadmap.path("id").asText()), 201);
        JsonNode maximumNext = postJson("/api/check-ins", maximum,
                checkIn(maximumPlan, 1, 168, outcomes(maximumPlan, "MISSED")), 201).path("nextPlan");
        assertThat(maximumNext.path("capacityHours").asInt()).isEqualTo(168);
        assertThat(maximumNext.path("plannedHours").asInt()).isLessThanOrEqualTo(168);
        assertThat(maximumNext.path("tasks").size()).isLessThanOrEqualTo(3);
    }

    @Test
    void historyIsPagedNewestFirstOwnershipSafeAndAllEndpointsRequireAuthentication() throws Exception {
        String owner = register(8);
        JsonNode roadmap = createRoadmap(owner);
        JsonNode plan = postJson("/api/weekly-plan", owner, Map.of("roadmapId", roadmap.path("id").asText()), 201);
        String other = register(8);
        assertThat(getJson("/api/check-ins/history?page=0", owner, 200).path("items")).hasSize(1);
        assertThat(getJson("/api/check-ins/history?page=0", other, 200).path("items")).isEmpty();
        postJson("/api/check-ins", other, checkIn(plan, 0, 8, outcomes(plan, "MISSED")), 404);
        mvc.perform(get("/api/weekly-plan/current")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/check-ins/current")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/check-ins/history")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/check-ins").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        getJson("/api/weekly-plan/current?weekStart=2026-09-15", owner, 400);
        getJson("/api/weekly-plan/current?weekStart=2026-09-21", owner, 400);
        getJson("/api/check-ins/history?page=-1", owner, 400);
    }

    @Test
    void duplicateCheckInLeavesRoadmapAndNextPlanUnchanged() throws Exception {
        String token = register(8);
        JsonNode roadmap = createRoadmap(token);
        JsonNode plan = postJson("/api/weekly-plan", token, Map.of("roadmapId", roadmap.path("id").asText()), 201);
        Map<String, Object> request = checkIn(plan, 2, 8, outcomes(plan, "PARTIAL"));
        JsonNode first = postJson("/api/check-ins", token, request, 201);
        JsonNode afterFirst = getJson("/api/roadmaps/" + roadmap.path("id").asText(), token, 200);
        postJson("/api/check-ins", token, request, 409);
        assertThat(getJson("/api/roadmaps/" + roadmap.path("id").asText(), token, 200)).isEqualTo(afterFirst);
        clock.set(Instant.parse("2026-09-23T12:00:00Z"));
        assertThat(getJson("/api/weekly-plan/current?weekStart=2026-09-21", token, 200).path("id"))
                .isEqualTo(first.path("nextPlan").path("id"));
    }

    private Map<String, Object> checkIn(JsonNode plan, int actual, int next, List<Map<String, Object>> outcomes) {
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("planId", plan.path("id").asText()); result.put("expectedRoadmapRevision", plan.path("roadmapRevision").asLong());
        result.put("actualHours", actual); result.put("availableHoursNextWeek", next); result.put("difficultyRating", 3);
        result.put("confidenceRating", 4); result.put("energyOrCapacityBand", "MEDIUM"); result.put("blockers", List.of("NO_TIME"));
        result.put("notes", "A concise weekly note."); result.put("constraint", null); result.put("tasks", outcomes);
        return result;
    }
    private List<Map<String, Object>> outcomes(JsonNode plan, String outcome) {
        List<Map<String, Object>> result = new ArrayList<>();
        plan.path("tasks").forEach(task -> result.add(Map.of("taskId", task.path("taskId").asText(), "outcome", outcome)));
        return result;
    }
    private JsonNode findTask(JsonNode roadmap, String id) {
        for (JsonNode phase : roadmap.path("phases")) for (JsonNode task : phase.path("tasks")) if (task.path("id").asText().equals(id)) return task;
        throw new AssertionError("Task not found");
    }
    private JsonNode findTaskByName(JsonNode roadmap, String name) {
        for (JsonNode phase : roadmap.path("phases")) for (JsonNode task : phase.path("tasks"))
            if (task.path("skillName").asText().equals(name)) return task;
        throw new AssertionError("Task not found");
    }
    private JsonNode createRoadmap(String token) throws Exception { return postJson("/api/roadmaps", token, Map.of("careerId", CAREER), 201); }
    private JsonNode postJson(String path, String token, Object body, int expected) throws Exception {
        String response = mvc.perform(post(path).header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body))).andExpect(status().is(expected)).andReturn().getResponse().getContentAsString();
        return response.isBlank() ? mapper.createObjectNode() : mapper.readTree(response);
    }
    private JsonNode getJson(String path, String token, int expected) throws Exception {
        String response = mvc.perform(get(path).header("Authorization", token)).andExpect(status().is(expected))
                .andReturn().getResponse().getContentAsString();
        return response.isBlank() ? mapper.createObjectNode() : mapper.readTree(response);
    }
    private String register(int hours) throws Exception {
        String body = mapper.writeValueAsString(Map.of("displayName", "Progress Student", "email", UUID.randomUUID() + "@example.com", "password", "demo-progress-test-password"));
        String token = "Bearer " + mapper.readTree(mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).path("accessToken").asText();
        mvc.perform(put("/api/profile").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"timeAvailablePerWeek\":" + hours + "}")).andExpect(status().isOk());
        return token;
    }

    @TestConfiguration static class TimeConfig {
        @Bean @Primary MutableClock testClock() { return new MutableClock(Instant.parse("2026-09-16T12:00:00Z")); }
    }
    static final class MutableClock extends Clock {
        private Instant instant;
        MutableClock(Instant instant) { this.instant = instant; }
        void set(Instant instant) { this.instant = instant; }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return instant; }
    }
}
