package com.mentorai.progress.entity;
import jakarta.persistence.*; import java.time.LocalDate; import java.util.UUID;
@Entity @Table(name="weekly_temporary_constraints")
public class WeeklyTemporaryConstraint {
    @Id @Column(name="check_in_id") private UUID checkInId;
    @OneToOne(fetch=FetchType.LAZY) @MapsId @JoinColumn(name="check_in_id") private WeeklyCheckIn checkIn;
    @Enumerated(EnumType.STRING) @Column(name="constraint_type",nullable=false,length=40) private ProgressEnums.ConstraintType type;
    @Column(name="start_date",nullable=false) private LocalDate startDate;
    @Column(name="end_date",nullable=false) private LocalDate endDate;
    protected WeeklyTemporaryConstraint() { }
    WeeklyTemporaryConstraint(WeeklyCheckIn checkIn,ProgressEnums.ConstraintType type,LocalDate startDate,LocalDate endDate){this.checkIn=checkIn;this.type=type;this.startDate=startDate;this.endDate=endDate;}
    public ProgressEnums.ConstraintType getType(){return type;} public LocalDate getStartDate(){return startDate;} public LocalDate getEndDate(){return endDate;}
}
