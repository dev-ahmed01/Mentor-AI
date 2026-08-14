package com.mentorai.skills.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "skills")
public class Skill {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "normalized_name", nullable = false, unique = true, length = 100)
    private String normalizedName;

    @Column(nullable = false, length = 60)
    private String category;

    @Column(length = 1000)
    private String description;

    protected Skill() {
    }

    public Skill(String name, String normalizedName, String category) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.normalizedName = normalizedName;
        this.category = category;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }
}
