package com.mentorai.progress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
@Import(WeeklyProgressIntegrationTest.TimeConfig.class)
class AdaptationIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired WeeklyProgressIntegrationTest.MutableClock clock;
    @BeforeEach void resetClock() { clock.set(Instant.parse("2026-09-16T12:00:00Z")); }

    @Test void behindPlanIsProposedThenExplicitlyAcceptedWithoutChangingCareerOrProfile() throws Exception {
        String token = register(20);
        JsonNode profile = get("/api/profile", token, 200);
        JsonNode plan = start(token);
        JsonNode result = submit(token, plan, 4, 20, "MISSED", null);
        JsonNode proposal = result.path("adaptation");
        assertThat(proposal.path("status").asText()).isEqualTo("PENDING");
        assertThat(proposal.path("trigger").asText()).isEqualTo("BEHIND");
        assertThat(proposal.path("proposed").path("capacityHours").asInt()).isEqualTo(4);
        assertThat(proposal.path("proposed").path("tasks").size()).isLessThan(plan.path("tasks").size());
        assertThat(result.path("nextPlan").path("capacityHours").asInt()).isEqualTo(20);
        JsonNode accepted = accept(token, proposal, null, 200);
        assertThat(accepted.path("status").asText()).isEqualTo("ACCEPTED");
        assertThat(accepted.path("accepted")).isEqualTo(proposal.path("proposed"));
        assertThat(accepted.path("before")).isEqualTo(proposal.path("before"));
        assertThat(get("/api/profile", token, 200)).isEqualTo(profile);
        JsonNode after = get("/api/roadmaps/" + plan.path("roadmapId").asText(), token, 200);
        assertThat(after.path("careerId").asText()).isEqualTo("20000000-0000-0000-0000-000000000001");
        accept(token, proposal, null, 409);
        JsonNode history = get("/api/roadmaps/" + plan.path("roadmapId").asText() + "/adaptations", token, 200);
        assertThat(history.path("items").get(0).path("id")).isEqualTo(proposal.path("id"));
    }

    @Test void genuineCompletionUnlocksNextFoundationButHoursAloneDoNotCompleteSkills() throws Exception {
        String token = register(16);
        JsonNode plan = start(token);
        JsonNode result = submit(token, plan, 16, 16, "COMPLETED", null);
        JsonNode proposal = result.path("adaptation");
        assertThat(proposal.path("trigger").asText()).isEqualTo("AHEAD");
        assertThat(proposal.path("proposed").path("tasks").findValuesAsText("title"))
                .anyMatch(title -> title.contains("Spring Fundamentals"));
        assertThat(proposal.path("proposed").path("tasks").findValuesAsText("taskId"))
                .doesNotContainAnyElementsOf(plan.path("tasks").findValuesAsText("taskId"));
    }

    @Test void maintenanceOffersLightReviewAndPreservesResumeWithoutChangingTaskStates() throws Exception {
        String token = register(20);
        JsonNode plan = start(token);
        Object constraint = Map.of("type", "EXAMS", "startDate", "2026-09-21", "endDate", "2026-09-27");
        JsonNode result = submit(token, plan, 4, 10, "PARTIAL", constraint);
        JsonNode proposal = result.path("adaptation");
        assertThat(proposal.path("proposed").path("mode").asText()).isEqualTo("MAINTENANCE");
        assertThat(proposal.path("proposed").path("capacityHours").asInt()).isEqualTo(2);
        assertThat(proposal.path("proposed").path("tasks")).hasSize(1);
        assertThat(proposal.path("resumeTaskId").asText()).isNotBlank();
        accept(token, proposal, null, 200);
        JsonNode roadmapBefore = get("/api/roadmaps/" + plan.path("roadmapId").asText(), token, 200);
        clock.set(Instant.parse("2026-09-23T12:00:00Z"));
        JsonNode maintenance = get("/api/weekly-plan/current", token, 200);
        JsonNode resumed = submit(token, maintenance, 2, 10, "COMPLETED", null);
        assertThat(get("/api/roadmaps/" + plan.path("roadmapId").asText(), token, 200)).isEqualTo(roadmapBefore);
        assertThat(resumed.path("adaptation").path("proposed").path("mode").asText()).isEqualTo("NORMAL");
        assertThat(resumed.path("adaptation").path("proposed").path("tasks").findValuesAsText("taskId"))
                .contains(proposal.path("resumeTaskId").asText());
    }

    @Test void acceptanceValidatesOwnershipHoursReadinessAndStaleRoadmap() throws Exception {
        String token = register(16);
        JsonNode plan = start(token);
        JsonNode proposal = submit(token, plan, 4, 10, "PARTIAL", null).path("adaptation");
        String other = register(8);
        get("/api/adaptations/" + proposal.path("id").asText(), other, 404);
        accept(other, proposal, null, 404);
        accept(token, proposal, Map.of("capacityHours", -1, "tasks", List.of()), 400);
        accept(token, proposal, Map.of("capacityHours", 2.5, "tasks", List.of()), 400);
        accept(token, proposal, Map.of("capacityHours", 8, "tasks", List.of(Map.of("taskId", UUID.randomUUID(), "plannedHours", 1))), 400);
        JsonNode roadmap = get("/api/roadmaps/" + plan.path("roadmapId").asText(), token, 200);
        call("PUT", "/api/roadmaps/" + plan.path("roadmapId").asText(), token,
                Map.of("expectedRevision", roadmap.path("revision").asLong(), "title", "Edited since proposal"), 200);
        accept(token, proposal, null, 409);
    }

    @Test void editedAcceptanceRecordsExactSnapshotAndRefusesCheckedInTarget() throws Exception {
        String token = register(16);
        JsonNode plan = start(token);
        JsonNode result = submit(token, plan, 4, 16, "MISSED", null);
        JsonNode proposal = result.path("adaptation");
        JsonNode accepted = accept(token, proposal, Map.of("capacityHours", 0, "tasks", List.of()), 200);
        assertThat(accepted.path("accepted").path("capacityHours").asInt()).isZero();
        assertThat(accepted.path("accepted").path("tasks")).isEmpty();
        assertThat(accepted.path("proposed")).isEqualTo(proposal.path("proposed"));

        String second = register(8);
        JsonNode first = start(second);
        JsonNode next = submit(second, first, 0, 8, "MISSED", null);
        clock.set(Instant.parse("2026-09-23T12:00:00Z"));
        submit(second, get("/api/weekly-plan/current", second, 200), 0, 8, "MISSED", null);
        accept(second, next.path("adaptation"), null, 409);
    }

    @Test void repeatedDeferralsPauseAutomaticReassignmentAndAskAboutBlocker() throws Exception {
        String token = register(8);
        JsonNode plan = start(token);
        String taskId = plan.path("tasks").get(0).path("taskId").asText();
        JsonNode proposal = null;
        for (int week = 0; week < 3; week++) {
            proposal = submit(token, plan, 0, 8, "DEFERRED", null).path("adaptation");
            if (week < 2) {
                accept(token, proposal, Map.of("capacityHours", 8,
                        "tasks", List.of(Map.of("taskId", taskId, "plannedHours", 8))), 200);
                clock.set(clock.instant().plus(7, java.time.temporal.ChronoUnit.DAYS));
                plan = get("/api/weekly-plan/current", token, 200);
            }
        }
        assertThat(proposal.path("blockerQuestions").findValuesAsText("taskId")).contains(taskId);
        assertThat(proposal.path("proposed").path("tasks").findValuesAsText("taskId")).doesNotContain(taskId);
    }

    @Test void staleCheckInCannotRecordAgainstAnAcceptedAllocationEvenWhenTaskIdsMatch() throws Exception {
        String token = register(8);
        JsonNode plan = start(token);
        JsonNode result = submit(token, plan, 4, 8, "PARTIAL", null);
        JsonNode stale = result.path("nextPlan");
        accept(token, result.path("adaptation"), null, 200);
        clock.set(Instant.parse("2026-09-23T12:00:00Z"));
        var body = Map.of("planId", stale.path("id").asText(), "expectedRoadmapRevision", stale.path("roadmapRevision").asLong(),
                "actualHours", 1, "availableHoursNextWeek", 8, "energyOrCapacityBand", "LOW", "blockers", List.of(),
                "tasks", List.of(Map.of("taskId", stale.path("tasks").get(0).path("taskId").asText(), "outcome", "MISSED")));
        call("POST", "/api/check-ins", token, body, 409);
    }

    @Test void knownSkillsStayOutOfNewLearningButCanBeReviewedWithoutReopening() throws Exception {
        String token = register(16);
        call("PUT", "/api/profile", token, Map.of("timeAvailablePerWeek", 16, "skills", List.of(Map.of("name", "Java", "category", "Language",
                "proficiency", "INTERMEDIATE", "confidence", "MEDIUM", "source", "SELF_REPORTED"))), 200);
        JsonNode plan = start(token);
        JsonNode roadmap = get("/api/roadmaps/" + plan.path("roadmapId").asText(), token, 200);
        JsonNode known = null;
        for (var phase : roadmap.path("phases")) for (var task : phase.path("tasks"))
            if (task.path("skillName").asText().equals("Java")) known=task;
        assertThat(known).isNotNull();
        assertThat(known.path("satisfiedAtGeneration").asBoolean()).isTrue();
        String knownId = known.path("id").asText();
        assertThat(plan.path("tasks").findValuesAsText("taskId")).doesNotContain(knownId);
        JsonNode result = submit(token, plan, 1, 8, "MISSED", Map.of("type", "EXAMS", "startDate", "2026-09-21", "endDate", "2026-10-04"));
        JsonNode proposal = result.path("adaptation");
        assertThat(proposal.path("proposed").path("tasks").findValuesAsText("taskId")).contains(knownId);
        accept(token, proposal, null, 200);
        clock.set(Instant.parse("2026-09-23T12:00:00Z"));
        JsonNode review = get("/api/weekly-plan/current", token, 200);
        JsonNode next = submit(token, review, 2, 10, "PARTIAL", null);
        assertThat(next.path("adaptation").path("proposed").path("mode").asText()).isEqualTo("MAINTENANCE");
        assertThat(get("/api/roadmaps/" + plan.path("roadmapId").asText(), token, 200)).isEqualTo(roadmap);
    }

    @Test void zeroCapacityAndNoReviewableWorkYieldEmptyPlansAndNonoverlappingConstraintsDoNotPauseLearning() throws Exception {
        String token = register(8);
        JsonNode plan = start(token);
        JsonNode result = submit(token, plan, 0, 0, "MISSED", Map.of("type", "EXAMS", "startDate", "2026-10-01", "endDate", "2026-10-04"));
        assertThat(result.path("adaptation").path("proposed").path("mode").asText()).isEqualTo("NORMAL");
        assertThat(result.path("adaptation").path("proposed").path("tasks")).isEmpty();
        String other = register(8);
        JsonNode otherPlan = start(other);
        JsonNode rest = submit(other, otherPlan, 0, 8, "MISSED", Map.of("type", "EXAMS", "startDate", "2026-09-21", "endDate", "2026-09-27"));
        assertThat(rest.path("adaptation").path("proposed").path("mode").asText()).isEqualTo("MAINTENANCE");
        assertThat(rest.path("adaptation").path("proposed").path("tasks")).isEmpty();
        assertThat(rest.path("adaptation").path("resumeTaskId").asText()).isNotBlank();
    }

    @Test void editRejectsDuplicateBlockedOverallocatedAndExcessiveMaintenanceTasks() throws Exception {
        String token = register(16);
        JsonNode plan = start(token);
        JsonNode proposal = submit(token, plan, 2, 16, "PARTIAL", null).path("adaptation");
        var one = Map.of("taskId", proposal.path("candidates").get(0).path("taskId").asText(), "plannedHours", 2);
        accept(token, proposal, Map.of("capacityHours", 8, "tasks", List.of(one, one)), 400);
        accept(token, proposal, Map.of("capacityHours", 1, "tasks", List.of(one)), 400);
        accept(token, proposal, Map.of("capacityHours", 168, "tasks", List.of(Map.of("taskId", one.get("taskId"), "plannedHours", 168))), 400);
        JsonNode roadmap = get("/api/roadmaps/" + plan.path("roadmapId").asText(), token, 200);
        String blocked = "";
        for (var phase : roadmap.path("phases")) for (var task : phase.path("tasks")) if (!task.path("ready").asBoolean()) blocked=task.path("id").asText();
        assertThat(blocked).isNotBlank();
        accept(token, proposal, Map.of("capacityHours", 8, "tasks", List.of(Map.of("taskId", blocked, "plannedHours", 1))), 400);
        String other = register(16);
        JsonNode otherPlan = start(other);
        JsonNode maintenance = submit(other, otherPlan, 2, 8, "PARTIAL", Map.of("type", "EXAMS", "startDate", "2026-09-21", "endDate", "2026-09-27")).path("adaptation");
        accept(other, maintenance, Map.of("capacityHours", 3, "tasks", List.of()), 400);
        get("/api/roadmaps/" + plan.path("roadmapId").asText() + "/adaptations", other, 404);
        get("/api/roadmaps/" + plan.path("roadmapId").asText() + "/adaptations?page=-1", token, 400);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/adaptations/" + proposal.path("id").asText())).andExpect(status().isUnauthorized());
    }

    @Test void lateCheckInCannotProposeChangesToAnotherRoadmapAndExpiredProposalCannotBeAccepted() throws Exception {
        String token = register(8);
        JsonNode first = start(token);
        clock.set(Instant.parse("2026-09-23T12:00:00Z"));
        JsonNode second = start(token);
        JsonNode late = submit(token, first, 0, 2, "MISSED", null);
        assertThat(late.hasNonNull("adaptation")).isFalse();
        assertThat(late.path("nextPlan").path("id")).isEqualTo(second.path("id"));
        JsonNode proposal = submit(token, second, 0, 8, "MISSED", null).path("adaptation");
        clock.set(Instant.parse("2026-10-07T12:00:00Z"));
        assertThat(get("/api/adaptations/" + proposal.path("id").asText(), token, 200).path("canAccept").asBoolean()).isFalse();
        accept(token, proposal, null, 409);
    }

    @Test void concurrentAcceptanceAndCheckInSerializeSoOnlyOneCanUseTheOriginalAllocation() throws Exception {
        String token = register(8);
        JsonNode plan = start(token);
        JsonNode result = submit(token, plan, 4, 8, "PARTIAL", null);
        JsonNode proposal = result.path("adaptation");
        JsonNode target = result.path("nextPlan");
        clock.set(Instant.parse("2026-09-23T12:00:00Z"));
        var acceptBody = Map.of("expectedRoadmapRevision", proposal.path("roadmapRevision").asLong(), "expectedPlanRevision", proposal.path("planRevision").asLong());
        var checkBody = Map.of("planId", target.path("id").asText(), "expectedRoadmapRevision", target.path("roadmapRevision").asLong(), "expectedPlanRevision", target.path("revision").asLong(),
                "actualHours", 0, "availableHoursNextWeek", 8, "energyOrCapacityBand", "LOW", "blockers", List.of(),
                "tasks", List.of(Map.of("taskId", target.path("tasks").get(0).path("taskId").asText(), "outcome", "MISSED")));
        var gate = new java.util.concurrent.CyclicBarrier(2);
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var accept = executor.submit(() -> { gate.await(); return rawPost("/api/adaptations/" + proposal.path("id").asText() + "/accept", token, acceptBody); });
            var check = executor.submit(() -> { gate.await(); return rawPost("/api/check-ins", token, checkBody); });
            int a=accept.get(20, java.util.concurrent.TimeUnit.SECONDS), c=check.get(20, java.util.concurrent.TimeUnit.SECONDS);
            assertThat((a == 200 && c == 409) || (a == 409 && c == 201)).isTrue();
        }
    }

    @Test void hoursAloneDoNotCompleteTasksAndRevisionHistoryIsPagedAndOwnerOnly() throws Exception {
        String token = register(8);
        JsonNode plan = start(token);
        JsonNode roadmap = get("/api/roadmaps/" + plan.path("roadmapId").asText(), token, 200);
        for (int i=0; i<21; i++) {
            JsonNode result = submit(token, plan, 168, 8, "MISSED", null);
            assertThat(result.path("adaptation").path("trigger").asText()).isEqualTo("STEADY");
            plan = result.path("nextPlan");
            clock.set(clock.instant().plus(7, java.time.temporal.ChronoUnit.DAYS));
        }
        assertThat(get("/api/roadmaps/" + plan.path("roadmapId").asText(), token, 200)).isEqualTo(roadmap);
        String path = "/api/roadmaps/" + plan.path("roadmapId").asText() + "/adaptations";
        JsonNode first = get(path, token, 200), second = get(path + "?page=1", token, 200);
        assertThat(first.path("items")).hasSize(20);
        assertThat(first.path("hasNext").asBoolean()).isTrue();
        assertThat(second.path("items")).hasSize(1);
        assertThat(second.path("hasNext").asBoolean()).isFalse();
    }

    @Test void missedMaintenanceWeekResumesAtReportedCapacityOnceConstraintExpires() throws Exception {
        String token = register(16);
        JsonNode plan = start(token);
        JsonNode proposal = submit(token, plan, 4, 10, "PARTIAL", Map.of("type", "EXAMS", "startDate", "2026-09-21", "endDate", "2026-09-27")).path("adaptation");
        accept(token, proposal, null, 200);
        clock.set(Instant.parse("2026-09-23T12:00:00Z"));
        JsonNode review = get("/api/weekly-plan/current", token, 200);
        JsonNode resumed = submit(token, review, 0, 10, "MISSED", null).path("adaptation");
        assertThat(resumed.path("trigger").asText()).isEqualTo("RESUME");
        assertThat(resumed.path("proposed").path("capacityHours").asInt()).isEqualTo(10);
    }

    private int rawPost(String path, String token, Object body) throws Exception {
        return mvc.perform(post(path).header("Authorization",token).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body)))
                .andReturn().getResponse().getStatus();
    }

    private JsonNode start(String token) throws Exception {
        JsonNode roadmap = call("POST", "/api/roadmaps", token, Map.of("careerId", "20000000-0000-0000-0000-000000000001"), 201);
        return call("POST", "/api/weekly-plan", token, Map.of("roadmapId", roadmap.path("id").asText()), 201);
    }
    private JsonNode submit(String token, JsonNode plan, int actual, int capacity, String outcome, Object constraint) throws Exception {
        Map<String,Object> body = new LinkedHashMap<>();
        body.put("planId", plan.path("id").asText()); body.put("expectedRoadmapRevision", plan.path("roadmapRevision").asLong());
        body.put("expectedPlanRevision", plan.path("revision").asLong());
        body.put("actualHours", actual); body.put("availableHoursNextWeek", capacity); body.put("energyOrCapacityBand", "MEDIUM");
        body.put("blockers", List.of()); body.put("constraint", constraint);
        List<Object> tasks = new ArrayList<>();
        plan.path("tasks").forEach(t -> tasks.add(Map.of("taskId", t.path("taskId").asText(), "outcome", outcome)));
        body.put("tasks", tasks);
        return call("POST", "/api/check-ins", token, body, 201);
    }
    private JsonNode accept(String token, JsonNode proposal, Object edit, int expected) throws Exception {
        Map<String,Object> body = new LinkedHashMap<>();
        body.put("expectedRoadmapRevision", proposal.path("roadmapRevision").asLong());
        body.put("expectedPlanRevision", proposal.path("planRevision").asLong()); body.put("edit", edit);
        return call("POST", "/api/adaptations/" + proposal.path("id").asText() + "/accept", token, body, expected);
    }
    private JsonNode get(String path, String token, int expected) throws Exception { return call("GET", path, token, null, expected); }
    private JsonNode call(String method, String path, String token, Object body, int expected) throws Exception {
        var builder = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request(org.springframework.http.HttpMethod.valueOf(method), path)
                .header("Authorization", token).contentType(MediaType.APPLICATION_JSON);
        if (body != null) builder.content(mapper.writeValueAsString(body));
        String response = mvc.perform(builder).andExpect(status().is(expected)).andReturn().getResponse().getContentAsString();
        return response.isBlank() ? mapper.createObjectNode() : mapper.readTree(response);
    }
    private String register(int hours) throws Exception {
        JsonNode user = call("POST", "/api/auth/register", "", Map.of("displayName", "Adaptive student",
                "email", UUID.randomUUID() + "@example.com", "password", "phase-five-test-password"), 201);
        String token = "Bearer " + user.path("accessToken").asText();
        call("PUT", "/api/profile", token, Map.of("timeAvailablePerWeek", hours), 200);
        return token;
    }
}
