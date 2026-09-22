package com.mentorai.decision.service;

import com.mentorai.decision.dto.LearningDecision;
import com.mentorai.decision.dto.LearningPriority;
import com.mentorai.skills.dto.SkillPrerequisitesResponse;
import com.mentorai.skills.entity.SkillProficiency;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Versioned, illustrative ordering policy. Scores are not market or employment probabilities. */
@Service
public class LearningPriorityPolicy {
    public static final String VERSION = "learning-priorities-v1";

    public record Candidate(SkillPrerequisitesResponse readiness, SkillProficiency current,
                            SkillProficiency target, String relevance, int importance, int bottleneckCount) { }

    public int focusSlots(Integer weeklyHours) {
        if (weeklyHours == null || weeklyHours <= 0) return 0;
        return weeklyHours < 5 ? 1 : weeklyHours < 10 ? 2 : 3;
    }

    public List<LearningDecision> decide(List<Candidate> candidates, Integer weeklyHours) {
        return decide(candidates,weeklyHours,Map.of());
    }

    public List<LearningDecision> decide(List<Candidate> candidates, Integer weeklyHours, Map<UUID,Integer> marketBonuses) {
        boolean market=!marketBonuses.isEmpty();
        var ordered = candidates.stream().sorted(Comparator.comparingInt((Candidate item)->score(item,marketBonuses)).reversed()
                .thenComparing(item -> item.readiness().name())
                .thenComparing(item -> item.readiness().skillId())).toList();
        List<LearningDecision> result = new ArrayList<>();
        int eligibleRank = 0;
        int slots = focusSlots(weeklyHours);
        for (Candidate item : ordered) {
            var reasons = new ArrayList<String>();
            reasons.add(switch (item.relevance()) {
                case "REQUIRED" -> "TARGET_CAREER_REQUIRED_SKILL";
                case "PREFERRED" -> "TARGET_CAREER_PREFERRED_SKILL";
                case "REQUIRED_FOUNDATION" -> "FOUNDATION_FOR_REQUIRED_SKILL";
                default -> "FOUNDATION_FOR_PREFERRED_SKILL";
            });
            reasons.add(item.readiness().coverage().equals("NO_RECORDED_PREREQUISITES")
                    ? "NO_RECORDED_PREREQUISITES"
                    : item.readiness().eligible() ? "PREREQUISITES_MET" : "PREREQUISITES_MISSING");
            if (item.bottleneckCount() > 0) reasons.add("PREREQUISITE_BOTTLENECK");
            LearningPriority priority;
            if (targetMet(item)) {
                priority = LearningPriority.NOT_YET;
                reasons.add("ALREADY_PROFICIENT");
            } else if (!item.readiness().eligible()) {
                priority = LearningPriority.NOT_YET;
            } else {
                int rank = eligibleRank++;
                priority = rank < slots ? LearningPriority.LEARN_NOW
                        : rank < slots + 2 ? LearningPriority.LEARN_NEXT : LearningPriority.LEARN_LATER;
                if (rank >= slots) {
                    reasons.add(weeklyHours == null ? "WEEKLY_TIME_UNAVAILABLE" : "TIME_BUDGET_CONSTRAINT");
                    reasons.add("HIGHER_PRIORITY_FOCUS_FIRST");
                }
            }
            reasons.add(market?"PINNED_MARKET_SAMPLE":"MARKET_EVIDENCE_UNAVAILABLE");
            int distance = Math.max(0, item.target().ordinal() - (item.current() == null ? -1 : item.current().ordinal()));
            String effort = targetMet(item) ? "TARGET_MET"
                    : item.current() == SkillProficiency.BEGINNER ? "DEVELOPING" : "FOUNDATIONS";
            result.add(new LearningDecision(item.readiness().skillId(), item.readiness().name(), priority,
                    score(item,marketBonuses), item.readiness(), item.relevance(), item.importance(), item.current(),
                    item.target(), distance, effort, item.bottleneckCount(), List.copyOf(reasons), market?"PINNED_MARKET_SAMPLE":"MARKET_EVIDENCE_UNAVAILABLE"));
        }
        return List.copyOf(result);
    }

    private boolean targetMet(Candidate item) {
        return item.current() != null && item.current().ordinal() >= item.target().ordinal();
    }

    private int score(Candidate item) {
        if (targetMet(item)) return 0;
        int relevance = item.relevance().startsWith("REQUIRED") ? 50 : 25;
        int closeness = item.current() == SkillProficiency.BEGINNER ? 10
                : item.current() == SkillProficiency.AWARENESS ? 5 : 0;
        return Math.min(100, relevance + 4 * item.importance() + Math.min(20, 5 * item.bottleneckCount()) + closeness);
    }

    private int score(Candidate item,Map<UUID,Integer> bonuses) {
        return targetMet(item)?0:Math.min(100,score(item)+Math.clamp(bonuses.getOrDefault(item.readiness().skillId(),0),0,10));
    }
}
