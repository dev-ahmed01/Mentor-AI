package com.mentorai.progress.adaptation;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AdaptationResponse(UUID id, UUID checkInId, UUID roadmapId, UUID planId, LocalDate weekStart,
        String status, String policyVersion, String trigger, String reason, Instant createdAt, Instant acceptedAt,
        long roadmapRevision, long planRevision, boolean canAccept, Snapshot before, Snapshot proposed,
        Snapshot accepted, UUID resumeTaskId, String resumeTitle, List<Question> blockerQuestions,
        List<Candidate> candidates) {
    public record Task(UUID taskId, String title, int plannedHours) { }
    public record Snapshot(int capacityHours, String mode, List<Task> tasks) { }
    public record Candidate(UUID taskId, String title, int maxHours) { }
    public record Question(UUID taskId, String title, String question) { }
    public record Decision(String trigger, String reason, Snapshot proposed, UUID resumeTaskId,
                           String resumeTitle, List<Question> blockerQuestions, List<Candidate> candidates) { }
    public record History(List<AdaptationResponse> items, int page, boolean hasNext) { }
}
