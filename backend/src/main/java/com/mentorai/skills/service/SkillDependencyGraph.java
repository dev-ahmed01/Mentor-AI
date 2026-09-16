package com.mentorai.skills.service;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** A small, immutable-after-construction prerequisite graph with no profile or database dependency. */
public final class SkillDependencyGraph {
    public record Edge(UUID skillId, UUID prerequisiteId) { }

    private final Map<UUID, Set<UUID>> prerequisites = new LinkedHashMap<>();

    public SkillDependencyGraph(Collection<Edge> edges) {
        for (Edge edge : edges) {
            if (edge.skillId().equals(edge.prerequisiteId())) {
                throw new IllegalStateException("A skill cannot depend on itself.");
            }
            if (!prerequisites.computeIfAbsent(edge.skillId(), ignored -> new LinkedHashSet<>())
                    .add(edge.prerequisiteId())) {
                throw new IllegalStateException("Duplicate skill dependency.");
            }
        }
        validateAcyclic();
    }

    public Set<UUID> prerequisites(UUID skillId) {
        Set<UUID> result = new LinkedHashSet<>();
        var pending = new ArrayDeque<>(prerequisites.getOrDefault(skillId, Set.of()));
        while (!pending.isEmpty()) {
            UUID current = pending.removeFirst();
            if (result.add(current)) {
                pending.addAll(prerequisites.getOrDefault(current, Set.of()));
            }
        }
        return Set.copyOf(result);
    }

    private void validateAcyclic() {
        Map<UUID, Integer> remaining = new HashMap<>();
        Map<UUID, Set<UUID>> dependents = new HashMap<>();
        prerequisites.forEach((skill, required) -> {
            remaining.put(skill, required.size());
            required.forEach(prerequisite -> {
                remaining.putIfAbsent(prerequisite, 0);
                dependents.computeIfAbsent(prerequisite, ignored -> new LinkedHashSet<>()).add(skill);
            });
        });
        var ready = new ArrayDeque<UUID>();
        remaining.forEach((skill, count) -> { if (count == 0) ready.add(skill); });
        int visited = 0;
        while (!ready.isEmpty()) {
            UUID skill = ready.removeFirst();
            visited++;
            for (UUID dependent : dependents.getOrDefault(skill, Set.of())) {
                if (remaining.compute(dependent, (ignored, count) -> count - 1) == 0) {
                    ready.add(dependent);
                }
            }
        }
        if (visited != remaining.size()) {
            throw new IllegalStateException("Skill dependency graph contains a cycle.");
        }
    }
}
