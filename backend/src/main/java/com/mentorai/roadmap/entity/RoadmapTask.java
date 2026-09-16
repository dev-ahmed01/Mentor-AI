package com.mentorai.roadmap.entity;

import com.mentorai.skills.entity.SkillProficiency;
import jakarta.persistence.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "roadmap_tasks")
public class RoadmapTask {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "phase_id", nullable = false) private RoadmapPhase phase;
    @Column(name = "skill_id", nullable = false) private UUID skillId;
    @Column(name = "skill_name", nullable = false, length = 120) private String skillName;
    @Column(nullable = false) private int position;
    @Column(nullable = false, length = 200) private String title;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private TaskState state;
    @Enumerated(EnumType.STRING) @Column(name = "target_proficiency", nullable = false, length = 30) private SkillProficiency targetProficiency;
    @Column(name = "estimated_hours", nullable = false) private int estimatedHours;
    @Column(name = "satisfied_at_generation", nullable = false) private boolean satisfiedAtGeneration;
    @Column(name = "initial_priority", nullable = false, length = 30) private String initialPriority;
    @Column(name = "priority_points", nullable = false) private int priorityPoints;
    @Column(name = "ordering_reason", nullable = false, length = 1000) private String orderingReason;
    @ElementCollection
    @CollectionTable(name = "roadmap_task_prerequisites", joinColumns = @JoinColumn(name = "task_id"))
    @MapKeyColumn(name = "prerequisite_task_id")
    @Column(name = "snapshot_satisfied", nullable = false)
    private Map<UUID, Boolean> prerequisites = new LinkedHashMap<>();

    protected RoadmapTask() { }
    public RoadmapTask(RoadmapPhase phase, int position, UUID skillId, String skillName,
                       SkillProficiency target, int estimatedHours, boolean satisfied,
                       String initialPriority, int priorityPoints, String orderingReason) {
        this.id = UUID.randomUUID(); this.phase = phase; this.position = position;
        this.skillId = skillId; this.skillName = skillName; this.targetProficiency = target;
        this.title = "Practice " + skillName + " toward " + target.name().toLowerCase(java.util.Locale.ROOT);
        this.estimatedHours = estimatedHours; this.satisfiedAtGeneration = satisfied;
        this.state = satisfied ? TaskState.SKIPPED : TaskState.NOT_STARTED;
        this.initialPriority = initialPriority; this.priorityPoints = priorityPoints; this.orderingReason = orderingReason;
    }
    public void addPrerequisite(UUID taskId, boolean satisfied) { prerequisites.put(taskId, satisfied); }
    public void update(String title, int hours, TaskState state) { this.title = title; this.estimatedHours = hours; this.state = state; }
    public UUID getId() { return id; }
    public UUID getSkillId() { return skillId; }
    public String getSkillName() { return skillName; }
    public String getTitle() { return title; }
    public TaskState getState() { return state; }
    public SkillProficiency getTargetProficiency() { return targetProficiency; }
    public int getEstimatedHours() { return estimatedHours; }
    public boolean isSatisfiedAtGeneration() { return satisfiedAtGeneration; }
    public String getInitialPriority() { return initialPriority; }
    public int getPriorityPoints() { return priorityPoints; }
    public String getOrderingReason() { return orderingReason; }
    public Map<UUID, Boolean> getPrerequisites() { return prerequisites; }
}
