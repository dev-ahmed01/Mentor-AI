package com.mentorai.progress.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity @Table(name="weekly_check_ins")
public class WeeklyCheckIn {
    @Id private UUID id;
    @Column(name="plan_id",nullable=false,updatable=false,unique=true) private UUID planId;
    @Column(name="user_id",nullable=false,updatable=false) private UUID userId;
    @Column(name="roadmap_id",nullable=false,updatable=false) private UUID roadmapId;
    @Column(name="actual_hours",nullable=false) private int actualHours;
    @Column(name="available_hours_next_week",nullable=false) private int availableHoursNextWeek;
    @Column(name="difficulty_rating") private Integer difficultyRating;
    @Column(name="confidence_rating") private Integer confidenceRating;
    @Enumerated(EnumType.STRING) @Column(name="energy_band",nullable=false,length=20) private ProgressEnums.EnergyBand energyBand;
    @Column(length=500) private String notes;
    @Column(name="next_plan_id",nullable=false) private UUID nextPlanId;
    @Column(nullable=false,length=500) private String explanation;
    @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    @OneToMany(mappedBy="checkIn",cascade=CascadeType.ALL,orphanRemoval=true) @OrderBy("position ASC") private List<WeeklyTaskProgress> tasks=new ArrayList<>();
    @ElementCollection @CollectionTable(name="weekly_check_in_blockers",joinColumns=@JoinColumn(name="check_in_id"))
    @OrderColumn(name="position") @Enumerated(EnumType.STRING) @Column(name="blocker",nullable=false,length=40)
    private List<ProgressEnums.Blocker> blockers=new ArrayList<>();
    @OneToOne(mappedBy="checkIn",cascade=CascadeType.ALL,orphanRemoval=true) private WeeklyTemporaryConstraint constraint;
    protected WeeklyCheckIn() { }
    public WeeklyCheckIn(UUID planId,UUID userId,UUID roadmapId,int actualHours,int availableHoursNextWeek,Integer difficultyRating,
                         Integer confidenceRating,ProgressEnums.EnergyBand energyBand,List<ProgressEnums.Blocker> blockers,String notes,
                         UUID nextPlanId,String explanation,Instant now){
        this.id=UUID.randomUUID();this.planId=planId;this.userId=userId;this.roadmapId=roadmapId;this.actualHours=actualHours;
        this.availableHoursNextWeek=availableHoursNextWeek;this.difficultyRating=difficultyRating;this.confidenceRating=confidenceRating;
        this.energyBand=energyBand;this.blockers.addAll(blockers);this.notes=notes;this.nextPlanId=nextPlanId;
        this.explanation=explanation;this.createdAt=now.truncatedTo(ChronoUnit.MICROS);
    }
    public void addTask(UUID taskId,String title,ProgressEnums.Outcome outcome){tasks.add(new WeeklyTaskProgress(this,tasks.size(),taskId,title,outcome));}
    public void setConstraint(ProgressEnums.ConstraintType type,LocalDate start,LocalDate end){this.constraint=new WeeklyTemporaryConstraint(this,type,start,end);}
    public UUID getId(){return id;} public UUID getPlanId(){return planId;} public UUID getRoadmapId(){return roadmapId;}
    public int getActualHours(){return actualHours;} public int getAvailableHoursNextWeek(){return availableHoursNextWeek;}
    public Integer getDifficultyRating(){return difficultyRating;} public Integer getConfidenceRating(){return confidenceRating;}
    public ProgressEnums.EnergyBand getEnergyBand(){return energyBand;} public List<ProgressEnums.Blocker> getBlockers(){return blockers;}
    public String getNotes(){return notes;} public UUID getNextPlanId(){return nextPlanId;} public String getExplanation(){return explanation;}
    public Instant getCreatedAt(){return createdAt;} public List<WeeklyTaskProgress> getTasks(){return tasks;}
    public WeeklyTemporaryConstraint getConstraint(){return constraint;}
}
