package com.mentorai.progress.adaptation;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity @Table(name="roadmap_adaptations")
public class Adaptation {
    @Id private UUID id;
    @Column(name="user_id", nullable=false, updatable=false) private UUID userId;
    @Column(name="roadmap_id", nullable=false, updatable=false) private UUID roadmapId;
    @Column(name="check_in_id", nullable=false, updatable=false, unique=true) private UUID checkInId;
    @Column(name="plan_id", nullable=false, updatable=false) private UUID planId;
    @Column(name="roadmap_revision", nullable=false, updatable=false) private long roadmapRevision;
    @Column(name="plan_revision", nullable=false, updatable=false) private long planRevision;
    @Column(name="policy_version", nullable=false, length=60, updatable=false) private String policyVersion;
    @Column(nullable=false, length=20) private String status;
    @Column(name="before_json", nullable=false, columnDefinition="text", updatable=false) private String beforeJson;
    @Column(name="decision_json", nullable=false, columnDefinition="text", updatable=false) private String decisionJson;
    @Column(name="accepted_json", columnDefinition="text") private String acceptedJson;
    @Column(name="created_at", nullable=false, updatable=false) private Instant createdAt;
    @Column(name="accepted_at") private Instant acceptedAt;
    protected Adaptation() { }
    public Adaptation(UUID userId, UUID roadmapId, UUID checkInId, UUID planId, long roadmapRevision,
                      long planRevision, String beforeJson, String decisionJson, Instant now) {
        this.id=UUID.randomUUID(); this.userId=userId; this.roadmapId=roadmapId; this.checkInId=checkInId; this.planId=planId;
        this.roadmapRevision=roadmapRevision; this.planRevision=planRevision; this.policyVersion=AdaptationPolicy.VERSION;
        this.status="PENDING"; this.beforeJson=beforeJson; this.decisionJson=decisionJson; this.createdAt=now.truncatedTo(ChronoUnit.MICROS);
    }
    public void accept(String json, Instant now) { status="ACCEPTED"; acceptedJson=json; acceptedAt=now.truncatedTo(ChronoUnit.MICROS); }
    public UUID getId(){return id;} public UUID getRoadmapId(){return roadmapId;} public UUID getCheckInId(){return checkInId;}
    public UUID getPlanId(){return planId;} public long getRoadmapRevision(){return roadmapRevision;} public long getPlanRevision(){return planRevision;}
    public String getStatus(){return status;} public String getBeforeJson(){return beforeJson;} public String getDecisionJson(){return decisionJson;}
    public String getAcceptedJson(){return acceptedJson;} public String getPolicyVersion(){return policyVersion;}
    public Instant getCreatedAt(){return createdAt;} public Instant getAcceptedAt(){return acceptedAt;}
}
