package com.mentorai.progress.entity;
import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(name="weekly_plan_tasks")
public class WeeklyPlanTask {
    @Id private UUID id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="plan_id") private WeeklyPlan plan;
    @Column(name="task_id", nullable=false) private UUID taskId;
    @Column(nullable=false) private int position;
    @Column(nullable=false, length=200) private String title;
    @Column(name="planned_hours", nullable=false) private int plannedHours;
    protected WeeklyPlanTask() { }
    WeeklyPlanTask(WeeklyPlan plan,int position,UUID taskId,String title,int plannedHours){this.id=UUID.randomUUID();this.plan=plan;this.position=position;this.taskId=taskId;this.title=title;this.plannedHours=plannedHours;}
    public UUID getTaskId(){return taskId;} public String getTitle(){return title;} public int getPlannedHours(){return plannedHours;}
}
