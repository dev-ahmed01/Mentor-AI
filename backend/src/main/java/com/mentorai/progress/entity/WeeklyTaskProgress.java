package com.mentorai.progress.entity;
import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(name="weekly_task_progress")
public class WeeklyTaskProgress {
    @Id private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="check_in_id") private WeeklyCheckIn checkIn;
    @Column(name="task_id",nullable=false) private UUID taskId;
    @Column(nullable=false) private int position;
    @Column(nullable=false,length=200) private String title;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private ProgressEnums.Outcome outcome;
    protected WeeklyTaskProgress() { }
    WeeklyTaskProgress(WeeklyCheckIn checkIn,int position,UUID taskId,String title,ProgressEnums.Outcome outcome){this.id=UUID.randomUUID();this.checkIn=checkIn;this.position=position;this.taskId=taskId;this.title=title;this.outcome=outcome;}
    public UUID getTaskId(){return taskId;} public String getTitle(){return title;} public ProgressEnums.Outcome getOutcome(){return outcome;}
}
