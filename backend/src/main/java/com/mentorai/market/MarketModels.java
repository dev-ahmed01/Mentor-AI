package com.mentorai.market;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class MarketModels {
    private MarketModels() { }
    public record RawJob(String sourceId, String sourceUrl, String title, String company,
                         String location, boolean remote, Instant publishedAt, String description, String rawPayload) { }
    public record Mention(UUID skillId, String name, String requirement) { }
    public record Observation(UUID id, String source, String sourceId, String sourceUrl,
                              Instant collectedAt, Instant publishedAt, String title, String company,
                              String location, boolean remote, String processingVersion, List<Mention> skills) { }
    public record Frequency(UUID skillId, String name, int mentions, int required, int preferred, int unspecified) { }
    public record Snapshot(UUID id, UUID careerId, String careerName, String source, String sourceUrl,
                           String sourceContext, Instant collectedAt, Instant windowStart, Instant windowEnd,
                           Instant freshUntil, int sampleSize, int employerCount, int listingsWithSkills,
                           int minimumSampleSize, String processingVersion, List<String> matchedTitles,
                           List<Frequency> skills, List<Observation> observations, List<String> limitations) { }
    public record Evidence(String status, Snapshot snapshot, String message) { }
    public record SourceState(String source, Instant lastAttemptAt, Instant lastSuccessAt, String lastStatus) { }
    public record IngestionResult(String status, int received, int accepted, int rejected, int duplicates, int snapshots) { }
}
