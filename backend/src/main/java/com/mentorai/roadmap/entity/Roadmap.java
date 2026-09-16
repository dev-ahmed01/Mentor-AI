package com.mentorai.roadmap.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "roadmaps")
public class Roadmap {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false, updatable = false) private UUID userId;
    @Column(name = "career_id", nullable = false, updatable = false) private UUID careerId;
    @Column(name = "career_name", nullable = false, length = 160) private String careerName;
    @Column(nullable = false, length = 200) private String title;
    @Column(name = "weekly_hours", nullable = false) private int weeklyHours;
    @Column(name = "generation_version", nullable = false, length = 60) private String generationVersion;
    @Column(name = "decision_version", nullable = false, length = 60) private String decisionVersion;
    @Column(name = "profile_updated_at", nullable = false) private Instant profileUpdatedAt;
    @Column(name = "previous_roadmap_id", updatable = false) private UUID previousRoadmapId;
    @Version private Long revision;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @OneToMany(mappedBy = "roadmap", cascade = CascadeType.ALL)
    @OrderBy("position ASC") private List<RoadmapPhase> phases = new ArrayList<>();

    protected Roadmap() { }
    public Roadmap(UUID userId, UUID careerId, String careerName, int weeklyHours,
                   String generationVersion, String decisionVersion, Instant profileUpdatedAt, UUID previousRoadmapId) {
        this.id = UUID.randomUUID(); this.userId = userId; this.careerId = careerId; this.careerName = careerName;
        this.title = careerName + " learning roadmap"; this.weeklyHours = weeklyHours;
        this.generationVersion = generationVersion; this.decisionVersion = decisionVersion;
        this.profileUpdatedAt = profileUpdatedAt; this.previousRoadmapId = previousRoadmapId;
        this.createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS); this.updatedAt = createdAt;
    }
    public void addPhase(RoadmapPhase phase) { phases.add(phase); }
    public void setTitle(String title) { this.title = title; }
    public void touch() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        this.updatedAt = now.isAfter(updatedAt) ? now : updatedAt.plus(1, ChronoUnit.MICROS);
    }
    public UUID getId() { return id; }
    public UUID getCareerId() { return careerId; }
    public String getCareerName() { return careerName; }
    public String getTitle() { return title; }
    public int getWeeklyHours() { return weeklyHours; }
    public String getGenerationVersion() { return generationVersion; }
    public String getDecisionVersion() { return decisionVersion; }
    public Instant getProfileUpdatedAt() { return profileUpdatedAt; }
    public UUID getPreviousRoadmapId() { return previousRoadmapId; }
    public Long getRevision() { return revision; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<RoadmapPhase> getPhases() { return phases; }
}
