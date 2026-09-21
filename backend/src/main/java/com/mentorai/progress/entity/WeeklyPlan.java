package com.mentorai.progress.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity @Table(name = "weekly_plans", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "week_start"}))
public class WeeklyPlan {
    @Id private UUID id;
    @Column(name="user_id", nullable=false, updatable=false) private UUID userId;
    @Column(name="roadmap_id", nullable=false, updatable=false) private UUID roadmapId;
    @Column(name="roadmap_title", nullable=false, length=200) private String roadmapTitle;
    @Column(name="week_start", nullable=false, updatable=false) private LocalDate weekStart;
    @Column(name="capacity_hours", nullable=false) private int capacityHours;
    @Column(name="planned_hours", nullable=false) private int plannedHours;
    @Column(name="roadmap_revision", nullable=false) private long roadmapRevision;
    @Column(nullable=false, length=500) private String reason;
    @Column(name="created_at", nullable=false, updatable=false) private Instant createdAt;
    @OneToMany(mappedBy="plan", cascade=CascadeType.ALL, orphanRemoval=true) @OrderBy("position ASC")
    private List<WeeklyPlanTask> tasks = new ArrayList<>();
    protected WeeklyPlan() { }
    public WeeklyPlan(UUID userId, UUID roadmapId, String roadmapTitle, LocalDate weekStart, int capacityHours,
                      long roadmapRevision, String reason, Instant now) {
        this.id=UUID.randomUUID(); this.userId=userId; this.roadmapId=roadmapId; this.roadmapTitle=roadmapTitle;
        this.weekStart=weekStart; this.capacityHours=capacityHours; this.roadmapRevision=roadmapRevision;
        this.reason=reason; this.createdAt=now.truncatedTo(ChronoUnit.MICROS);
    }
    public void addTask(UUID taskId, String title, int hours) { tasks.add(new WeeklyPlanTask(this, tasks.size(), taskId, title, hours)); plannedHours += hours; }
    public UUID getId(){return id;} public UUID getUserId(){return userId;} public UUID getRoadmapId(){return roadmapId;}
    public String getRoadmapTitle(){return roadmapTitle;} public LocalDate getWeekStart(){return weekStart;}
    public int getCapacityHours(){return capacityHours;} public int getPlannedHours(){return plannedHours;}
    public long getRoadmapRevision(){return roadmapRevision;} public String getReason(){return reason;}
    public Instant getCreatedAt(){return createdAt;} public List<WeeklyPlanTask> getTasks(){return tasks;}
}
