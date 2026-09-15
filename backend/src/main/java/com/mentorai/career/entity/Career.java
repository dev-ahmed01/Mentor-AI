package com.mentorai.career.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "careers")
public class Career {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(nullable = false, length = 1500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_difficulty", nullable = false, length = 30)
    private EntryDifficulty entryDifficulty;

    @Column(name = "degree_relevance", nullable = false, length = 1000)
    private String degreeRelevance;

    @Column(name = "project_expectations", nullable = false, length = 1500)
    private String projectExpectations;

    @Column(name = "internship_expectations", nullable = false, length = 1500)
    private String internshipExpectations;

    @Column(name = "common_misconceptions", nullable = false, length = 1500)
    private String commonMisconceptions;

    @Column(name = "reality_summary", nullable = false, length = 2000)
    private String realitySummary;

    @Column(name = "recommended_weekly_hours", nullable = false)
    private int recommendedWeeklyHours;

    @Column(name = "demo_data", nullable = false)
    private boolean demoData;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "career_responsibilities", joinColumns = @JoinColumn(name = "career_id"))
    @Column(name = "responsibility", nullable = false, length = 400)
    @BatchSize(size = 20)
    private Set<String> responsibilities = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "career_job_titles", joinColumns = @JoinColumn(name = "career_id"))
    @Column(name = "job_title", nullable = false, length = 150)
    @BatchSize(size = 20)
    private Set<String> commonJobTitles = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "career_risks", joinColumns = @JoinColumn(name = "career_id"))
    @Column(name = "risk", nullable = false, length = 500)
    @BatchSize(size = 20)
    private Set<String> risks = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "career_market_considerations", joinColumns = @JoinColumn(name = "career_id"))
    @Column(name = "consideration", nullable = false, length = 500)
    @BatchSize(size = 20)
    private Set<String> marketConsiderations = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "career_interest_signals", joinColumns = @JoinColumn(name = "career_id"))
    @Column(name = "signal", nullable = false, length = 100)
    @BatchSize(size = 20)
    private Set<String> interestSignals = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "career_goal_signals", joinColumns = @JoinColumn(name = "career_id"))
    @Column(name = "signal", nullable = false, length = 100)
    @BatchSize(size = 20)
    private Set<String> goalSignals = new LinkedHashSet<>();

    @OneToMany(mappedBy = "career", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    private Set<CareerSkill> skills = new LinkedHashSet<>();

    protected Career() {
    }

    public UUID getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public EntryDifficulty getEntryDifficulty() {
        return entryDifficulty;
    }

    public String getDegreeRelevance() {
        return degreeRelevance;
    }

    public String getProjectExpectations() {
        return projectExpectations;
    }

    public String getInternshipExpectations() {
        return internshipExpectations;
    }

    public String getCommonMisconceptions() {
        return commonMisconceptions;
    }

    public String getRealitySummary() {
        return realitySummary;
    }

    public int getRecommendedWeeklyHours() {
        return recommendedWeeklyHours;
    }

    public boolean isDemoData() {
        return demoData;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Set<String> getResponsibilities() {
        return responsibilities;
    }

    public Set<String> getCommonJobTitles() {
        return commonJobTitles;
    }

    public Set<String> getRisks() {
        return risks;
    }

    public Set<String> getMarketConsiderations() {
        return marketConsiderations;
    }

    public Set<String> getInterestSignals() {
        return interestSignals;
    }

    public Set<String> getGoalSignals() {
        return goalSignals;
    }

    public Set<CareerSkill> getSkills() {
        return skills;
    }
}
