package com.mentorai.skills.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;

@Entity
@Table(name = "skill_dependencies", uniqueConstraints = @UniqueConstraint(
        name = "uk_skill_dependency_pair", columnNames = {"skill_id", "prerequisite_skill_id"}))
public class SkillDependency {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prerequisite_skill_id", nullable = false)
    private Skill prerequisite;
    @Column(nullable = false)
    private int importance;

    protected SkillDependency() { }

    public UUID getId() { return id; }
    public Skill getSkill() { return skill; }
    public Skill getPrerequisite() { return prerequisite; }
    public int getImportance() { return importance; }
}
