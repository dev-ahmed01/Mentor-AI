package com.mentorai.roadmap.service;

import com.mentorai.decision.dto.LearningDecision;
import com.mentorai.decision.dto.LearningPrioritiesResponse;
import com.mentorai.roadmap.entity.Roadmap;
import com.mentorai.roadmap.entity.RoadmapPhase;
import com.mentorai.roadmap.entity.RoadmapTask;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RoadmapGenerator {
    public static final String VERSION = "roadmap-generation-v1";

    public Roadmap generate(UUID owner, LearningPrioritiesResponse priorities, Instant profileUpdatedAt, UUID previousId) {
        Roadmap roadmap = new Roadmap(owner, priorities.careerId(), priorities.careerName(), priorities.weeklyHours(),
                VERSION, priorities.calculationVersion(), profileUpdatedAt, previousId);
        List<LearningDecision> ordered = order(priorities.decisions());
        Map<UUID, RoadmapTask> bySkill = new HashMap<>();
        RoadmapPhase phase = null;
        for (int i = 0; i < ordered.size(); i++) {
            if (i % 3 == 0) { phase = new RoadmapPhase(roadmap, i / 3); roadmap.addPhase(phase); }
            LearningDecision item = ordered.get(i);
            boolean known = item.reasonCodes().contains("ALREADY_PROFICIENT");
            int effort = known ? 0 : item.estimatedEffortBand().equals("DEVELOPING") ? 4 : 8;
            String reason = "Selected career relevance: " + item.careerRelevance().toLowerCase(java.util.Locale.ROOT).replace('_', ' ')
                    + ". Priority points: " + item.deterministicScore()
                    + ". Prerequisite tasks appear first; ties use skill name. Effort is an editable demo estimate.";
            RoadmapTask task = new RoadmapTask(phase, i % 3, item.skillId(), item.name(), item.targetProficiency(),
                    effort, known, item.priority().name(), item.deterministicScore(), reason);
            phase.addTask(task); bySkill.put(item.skillId(), task);
        }
        for (LearningDecision item : ordered) {
            for (var prerequisite : item.prerequisiteReadiness().prerequisites()) {
                bySkill.get(item.skillId()).addPrerequisite(bySkill.get(prerequisite.skillId()).getId(), prerequisite.satisfied());
            }
        }
        return roadmap;
    }

    private List<LearningDecision> order(List<LearningDecision> decisions) {
        List<LearningDecision> remaining = new ArrayList<>(decisions);
        Comparator<LearningDecision> rank = Comparator.comparingInt(LearningDecision::deterministicScore).reversed()
                .thenComparing(LearningDecision::name).thenComparing(LearningDecision::skillId);
        Set<UUID> emitted = new HashSet<>();
        List<LearningDecision> result = new ArrayList<>();
        while (!remaining.isEmpty()) {
            LearningDecision next = remaining.stream().filter(item -> item.prerequisiteReadiness().prerequisites().stream()
                    .allMatch(prerequisite -> emitted.contains(prerequisite.skillId()))).min(rank)
                    .orElseThrow(() -> new IllegalStateException("Roadmap prerequisites are cyclic or incomplete."));
            result.add(next); emitted.add(next.skillId()); remaining.remove(next);
        }
        return result;
    }
}
