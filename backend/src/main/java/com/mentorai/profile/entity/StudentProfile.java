package com.mentorai.profile.entity;

import com.mentorai.auth.entity.User;
import com.mentorai.skills.entity.StudentSkill;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "student_profiles")
public class StudentProfile {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 120)
    private String degree;

    @Column(name = "study_year")
    private Integer year;

    private Integer semester;

    @Column(name = "short_term_goal", length = 1000)
    private String shortTermGoal;

    @Column(name = "long_term_goal", length = 1000)
    private String longTermGoal;

    @Column(name = "experience_summary", length = 2000)
    private String experience;

    @Enumerated(EnumType.STRING)
    @Column(name = "remote_preference", length = 30)
    private RemotePreference remotePreference;

    @Column(name = "time_available_per_week")
    private Integer timeAvailablePerWeek;

    @ElementCollection
    @CollectionTable(name = "profile_interests", joinColumns = @JoinColumn(name = "profile_id"))
    @OrderColumn(name = "position")
    @Column(name = "interest", nullable = false, length = 120)
    private List<String> interests = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "profile_goals", joinColumns = @JoinColumn(name = "profile_id"))
    @OrderColumn(name = "position")
    @Column(name = "goal", nullable = false, length = 300)
    private List<String> goals = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "profile_programming_languages", joinColumns = @JoinColumn(name = "profile_id"))
    @OrderColumn(name = "position")
    @Column(name = "programming_language", nullable = false, length = 100)
    private List<String> programmingLanguages = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "profile_preferred_domains", joinColumns = @JoinColumn(name = "profile_id"))
    @OrderColumn(name = "position")
    @Column(name = "preferred_domain", nullable = false, length = 120)
    private List<String> preferredDomains = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "profile_target_locations", joinColumns = @JoinColumn(name = "profile_id"))
    @OrderColumn(name = "position")
    @Column(name = "target_location", nullable = false, length = 120)
    private List<String> targetLocations = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "profile_projects", joinColumns = @JoinColumn(name = "profile_id"))
    @OrderColumn(name = "position")
    @Column(name = "project", nullable = false, length = 300)
    private List<String> currentProjects = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "profile_certifications", joinColumns = @JoinColumn(name = "profile_id"))
    @OrderColumn(name = "position")
    @Column(name = "certification", nullable = false, length = 200)
    private List<String> certifications = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "profile_avoidances", joinColumns = @JoinColumn(name = "profile_id"))
    @OrderColumn(name = "position")
    @Column(name = "avoidance", nullable = false, length = 200)
    private List<String> avoidances = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentSkill> skills = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected StudentProfile() {
    }

    public StudentProfile(User user) {
        this.id = UUID.randomUUID();
        this.user = user;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public String getShortTermGoal() {
        return shortTermGoal;
    }

    public void setShortTermGoal(String shortTermGoal) {
        this.shortTermGoal = shortTermGoal;
    }

    public String getLongTermGoal() {
        return longTermGoal;
    }

    public void setLongTermGoal(String longTermGoal) {
        this.longTermGoal = longTermGoal;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public RemotePreference getRemotePreference() {
        return remotePreference;
    }

    public void setRemotePreference(RemotePreference remotePreference) {
        this.remotePreference = remotePreference;
    }

    public Integer getTimeAvailablePerWeek() {
        return timeAvailablePerWeek;
    }

    public void setTimeAvailablePerWeek(Integer timeAvailablePerWeek) {
        this.timeAvailablePerWeek = timeAvailablePerWeek;
    }

    public List<String> getInterests() {
        return interests;
    }

    public List<String> getGoals() {
        return goals;
    }

    public List<String> getProgrammingLanguages() {
        return programmingLanguages;
    }

    public List<String> getPreferredDomains() {
        return preferredDomains;
    }

    public List<String> getTargetLocations() {
        return targetLocations;
    }

    public List<String> getCurrentProjects() {
        return currentProjects;
    }

    public List<String> getCertifications() {
        return certifications;
    }

    public List<String> getAvoidances() {
        return avoidances;
    }

    public List<StudentSkill> getSkills() {
        return skills;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void replaceSkills(List<StudentSkill> replacements) {
        skills.clear();
        skills.addAll(replacements);
    }
}
