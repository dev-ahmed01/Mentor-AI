package com.mentorai.skills.entity;

import com.mentorai.profile.entity.StudentProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;

@Entity
@Table(name = "student_skills", uniqueConstraints =
        @UniqueConstraint(name = "uk_student_skill_profile_skill", columnNames = {"profile_id", "skill_id"}))
public class StudentSkill {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private StudentProfile profile;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SkillProficiency proficiency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SkillConfidence confidence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SkillSource source;

    protected StudentSkill() {
    }

    public StudentSkill(
            StudentProfile profile,
            Skill skill,
            SkillProficiency proficiency,
            SkillConfidence confidence,
            SkillSource source) {
        this.id = UUID.randomUUID();
        this.profile = profile;
        this.skill = skill;
        this.proficiency = proficiency;
        this.confidence = confidence;
        this.source = source;
    }

    public UUID getId() {
        return id;
    }

    public Skill getSkill() {
        return skill;
    }

    public SkillProficiency getProficiency() {
        return proficiency;
    }

    public SkillConfidence getConfidence() {
        return confidence;
    }

    public SkillSource getSource() {
        return source;
    }
}
