package com.mentorai.career.entity;

import com.mentorai.skills.entity.Skill;
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
@Table(name = "career_skills", uniqueConstraints =
        @UniqueConstraint(name = "uk_career_skill_career_skill", columnNames = {"career_id", "skill_id"}))
public class CareerSkill {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "career_id", nullable = false)
    private Career career;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(nullable = false)
    private int importance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SkillRequirement requirement;

    protected CareerSkill() {
    }

    public UUID getId() {
        return id;
    }

    public Skill getSkill() {
        return skill;
    }

    public int getImportance() {
        return importance;
    }

    public SkillRequirement getRequirement() {
        return requirement;
    }
}
