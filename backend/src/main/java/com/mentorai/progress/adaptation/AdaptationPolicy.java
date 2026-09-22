package com.mentorai.progress.adaptation;

import com.mentorai.decision.service.LearningPriorityPolicy;
import com.mentorai.progress.entity.*;
import com.mentorai.progress.entity.ProgressEnums.Outcome;
import com.mentorai.roadmap.dto.RoadmapResponse;
import com.mentorai.roadmap.entity.TaskState;
import java.util.*;
import org.springframework.stereotype.Component;
import static com.mentorai.progress.adaptation.AdaptationResponse.*;

@Component
public class AdaptationPolicy {
    public static final String VERSION = "weekly-adaptation-v1";
    private final LearningPriorityPolicy priorities;
    public AdaptationPolicy(LearningPriorityPolicy priorities) { this.priorities = priorities; }

    // History is chronological by planned week, through the triggering week only.
    public Decision propose(RoadmapResponse roadmap, WeeklyPlan source, WeeklyPlan target,
                            WeeklyCheckIn checkIn, List<WeeklyCheckIn> history) {
        var all = roadmap.phases().stream().flatMap(p -> p.tasks().stream()).toList();
        Set<UUID> prerequisiteIds = new HashSet<>();
        all.stream().filter(t -> !t.state().terminal()).forEach(t -> t.prerequisites().stream()
                .filter(p -> !p.satisfied()).forEach(p -> prerequisiteIds.add(p.taskId())));
        var ready = all.stream().filter(t -> !t.state().terminal() && t.ready())
                .sorted(Comparator.comparingInt((RoadmapResponse.Task t) -> prerequisiteIds.contains(t.id()) ? 0 : 1)
                        .thenComparingInt(t -> t.state() == TaskState.IN_PROGRESS ? 0 : 1)).toList();
        var resume = ready.isEmpty() ? null : ready.getFirst();
        Map<UUID, Integer> deferrals = new HashMap<>();
        for (var earlier : history) for (var task : earlier.getTasks()) {
            if (task.getOutcome() == Outcome.DEFERRED) deferrals.merge(task.getTaskId(), 1, Integer::sum);
            else if (task.getOutcome() == Outcome.PARTIAL || task.getOutcome() == Outcome.COMPLETED) deferrals.remove(task.getTaskId());
        }
        Set<UUID> excluded = new HashSet<>();
        List<Question> questions = new ArrayList<>();
        for (var task : all) if (!task.state().terminal() && deferrals.getOrDefault(task.id(), 0) >= 3) {
            excluded.add(task.id());
            questions.add(new Question(task.id(), task.title(), "This has been deferred three or more times. What would help: a smaller step, a clearer resource, or a pause? Select it below only when you want to resume."));
        }
        checkIn.getTasks().stream().filter(t -> t.getOutcome() == Outcome.DEFERRED).forEach(t -> excluded.add(t.getTaskId()));
        var constraint = history.stream().map(WeeklyCheckIn::getConstraint).filter(Objects::nonNull)
                .filter(c -> !c.getStartDate().isAfter(target.getWeekStart().plusDays(6)) && !c.getEndDate().isBefore(target.getWeekStart()))
                .reduce((first, last) -> last).orElse(null);
        int capacity = checkIn.getAvailableHoursNextWeek();
        boolean completedAll = !source.getTasks().isEmpty() && checkIn.getTasks().stream().allMatch(t -> t.getOutcome() == Outcome.COMPLETED);
        boolean behind = !source.getMode().equals("MAINTENANCE") && source.getPlannedHours() > 0
                && checkIn.getActualHours() * 5 < source.getPlannedHours() * 3 && !completedAll;
        String mode = constraint == null ? "NORMAL" : "MAINTENANCE";
        String trigger;
        String reason;
        List<RoadmapResponse.Task> eligible;
        int slots;
        if (constraint != null) {
            capacity = Math.min(capacity, 2); slots = 1; trigger = "MAINTENANCE";
            reason = "A temporary " + constraint.getType().name().toLowerCase(Locale.ROOT).replace('_', ' ')
                    + " constraint overlaps this week. Pause new major learning; offer up to two hours of review. The learning resume point is preserved.";
            eligible = all.stream().filter(t -> t.state() == TaskState.IN_PROGRESS || t.state() == TaskState.NEEDS_REVIEW
                    || t.state() == TaskState.COMPLETED || t.satisfiedAtGeneration()).toList();
        } else {
            eligible = ready;
            if (behind) {
                capacity = Math.min(capacity, Math.max(1, checkIn.getActualHours()));
                slots = Math.min(priorities.focusSlots(capacity), Math.max(1, source.getTasks().size() - 1));
                trigger = "BEHIND";
                reason = "Recorded " + checkIn.getActualHours() + " of " + source.getPlannedHours()
                        + " planned hours. Suggest a smaller week, keeping the highest-priority unfinished prerequisite first. Other work stays in the roadmap without stacking the backlog.";
            } else {
                slots = priorities.focusSlots(capacity);
                trigger = "MAINTENANCE".equals(source.getMode()) ? "RESUME" : completedAll ? "AHEAD" : "STEADY";
                reason = "RESUME".equals(trigger) ? "The temporary constraint has ended. Resume ready unfinished learning at your reported capacity."
                        : completedAll ? "Explicitly completed work unlocked the next relevant tasks. Move forward within your reported capacity."
                        : "Use your reported capacity and ready unfinished tasks. Your career goal stays unchanged.";
            }
        }
        List<Candidate> candidates = eligible.stream().map(t -> new Candidate(t.id(),
                mode.equals("MAINTENANCE") ? reviewTitle(t.title()) : t.title(), mode.equals("MAINTENANCE") ? 2 : t.estimatedHours())).toList();
        List<Task> tasks = new ArrayList<>(); int remaining = capacity;
        for (var candidate : candidates) {
            if (remaining <= 0 || tasks.size() >= slots) break;
            if (excluded.contains(candidate.taskId())) continue;
            int hours = Math.min(remaining, candidate.maxHours());
            tasks.add(new Task(candidate.taskId(), candidate.title(), hours)); remaining -= hours;
        }
        return new Decision(trigger, reason, new Snapshot(capacity, mode, List.copyOf(tasks)),
                resume == null ? null : resume.id(), resume == null ? null : resume.title(), List.copyOf(questions), candidates);
    }

    public static String reviewTitle(String title) {
        String value = "Review: " + title;
        return value.length() > 200 ? value.substring(0, 200) : value;
    }
}
