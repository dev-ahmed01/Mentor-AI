package com.mentorai.roadmap.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "roadmap_phases")
public class RoadmapPhase {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "roadmap_id", nullable = false) private Roadmap roadmap;
    @Column(nullable = false) private int position;
    @Column(nullable = false, length = 200) private String title;
    @OneToMany(mappedBy = "phase", cascade = CascadeType.ALL)
    @OrderBy("position ASC") private List<RoadmapTask> tasks = new ArrayList<>();

    protected RoadmapPhase() { }
    public RoadmapPhase(Roadmap roadmap, int position) {
        this.id = UUID.randomUUID(); this.roadmap = roadmap; this.position = position;
        this.title = "Learning stage " + (position + 1);
    }
    public void addTask(RoadmapTask task) { tasks.add(task); }
    public UUID getId() { return id; }
    public int getPosition() { return position; }
    public String getTitle() { return title; }
    public List<RoadmapTask> getTasks() { return tasks; }
}
