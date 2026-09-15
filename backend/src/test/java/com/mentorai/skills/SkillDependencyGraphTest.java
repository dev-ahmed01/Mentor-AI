package com.mentorai.skills;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mentorai.skills.service.SkillDependencyGraph;
import com.mentorai.skills.service.SkillDependencyGraph.Edge;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SkillDependencyGraphTest {
    private final UUID foundation = UUID.randomUUID();
    private final UUID left = UUID.randomUUID();
    private final UUID right = UUID.randomUUID();
    private final UUID target = UUID.randomUUID();

    @Test
    void traversesChainsAndDeduplicatesSharedAncestors() {
        var graph = new SkillDependencyGraph(List.of(new Edge(target, left), new Edge(target, right),
                new Edge(left, foundation), new Edge(right, foundation)));
        assertThat(graph.prerequisites(target)).containsExactlyInAnyOrder(left, right, foundation);
        assertThat(graph.prerequisites(foundation)).isEmpty();
        assertThat(graph.prerequisites(UUID.randomUUID())).isEmpty();
    }

    @Test
    void rejectsSelfEdgesDuplicatesAndIndirectCycles() {
        assertThatThrownBy(() -> new SkillDependencyGraph(List.of(new Edge(target, target))))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new SkillDependencyGraph(List.of(new Edge(target, left), new Edge(target, left))))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new SkillDependencyGraph(List.of(
                new Edge(target, left), new Edge(left, foundation), new Edge(foundation, target))))
                .isInstanceOf(IllegalStateException.class);
    }
}
