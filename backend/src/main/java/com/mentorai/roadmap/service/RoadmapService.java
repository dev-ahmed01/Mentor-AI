package com.mentorai.roadmap.service;

import com.mentorai.auth.service.AuthService;
import com.mentorai.common.exception.ConflictException;
import com.mentorai.common.exception.ProfileIncompleteException;
import com.mentorai.common.exception.ResourceNotFoundException;
import com.mentorai.decision.service.LearningDecisionService;
import com.mentorai.decision.service.LearningPriorityPolicy;
import com.mentorai.profile.service.ProfileService;
import com.mentorai.roadmap.dto.CreateRoadmapRequest;
import com.mentorai.roadmap.dto.RoadmapResponse;
import com.mentorai.roadmap.dto.UpdateRoadmapRequest;
import com.mentorai.roadmap.entity.Roadmap;
import com.mentorai.roadmap.entity.RoadmapTask;
import com.mentorai.roadmap.entity.TaskState;
import com.mentorai.roadmap.repository.RoadmapRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RoadmapService {
    private final RoadmapRepository roadmaps;
    private final AuthService auth;
    private final ProfileService profiles;
    private final LearningDecisionService decisions;
    private final LearningPriorityPolicy policy;
    private final RoadmapGenerator generator;

    public RoadmapService(RoadmapRepository roadmaps, AuthService auth, ProfileService profiles,
                          LearningDecisionService decisions, LearningPriorityPolicy policy, RoadmapGenerator generator) {
        this.roadmaps = roadmaps; this.auth = auth; this.profiles = profiles;
        this.decisions = decisions; this.policy = policy; this.generator = generator;
    }

    @Transactional
    public RoadmapResponse create(Authentication authentication, CreateRoadmapRequest request) {
        UUID owner = auth.requireUser(authentication).getId();
        var priorities = decisions.priorities(request.careerId(), authentication);
        if (priorities.weeklyHours() == null) throw new ProfileIncompleteException("Record weekly learning availability before generating a roadmap.");
        UUID previous = roadmaps.findFirstByUserIdOrderByCreatedAtDescIdDesc(owner).map(Roadmap::getId).orElse(null);
        Roadmap roadmap = generator.generate(owner, priorities, profiles.get(authentication).updatedAt(), previous);
        return describe(roadmaps.saveAndFlush(roadmap));
    }

    public RoadmapResponse current(Authentication authentication) {
        return describe(roadmaps.findFirstByUserIdOrderByCreatedAtDescIdDesc(auth.requireUser(authentication).getId())
                .orElseThrow(() -> new ResourceNotFoundException("No roadmap has been created yet.")));
    }
    public RoadmapResponse get(Authentication authentication, UUID id) { return describe(requireOwned(authentication, id)); }

    @Transactional
    public RoadmapResponse update(Authentication authentication, UUID id, UpdateRoadmapRequest request) {
        Roadmap roadmap = requireOwned(authentication, id);
        if (!roadmap.getRevision().equals(request.expectedRevision()))
            throw new ConflictException("This roadmap changed. Refresh it before saving your edits.");
        if (request.title() != null && request.title().isBlank()) throw new RoadmapValidationException("Roadmap title cannot be blank.");
        List<RoadmapTask> all = tasks(roadmap);
        Map<UUID, RoadmapTask> byId = all.stream().collect(Collectors.toMap(RoadmapTask::getId, Function.identity()));
        Map<UUID, TaskState> proposed = states(all);
        var edits = request.tasks() == null ? List.<UpdateRoadmapRequest.TaskEdit>of() : request.tasks();
        var seen = new HashSet<UUID>();
        for (var edit : edits) {
            RoadmapTask task = byId.get(edit.id());
            if (task == null || !seen.add(edit.id())) throw new RoadmapValidationException("Every edited task must occur once and belong to this roadmap.");
            if (!task.getState().canTransitionTo(edit.state())) throw new ConflictException("That task state transition is not allowed. Reopen completed work as Needs review.");
            if (!edit.state().terminal() && edit.estimatedHours() <= 0) throw new RoadmapValidationException("Unfinished tasks need a positive effort estimate.");
            proposed.put(edit.id(), edit.state());
        }
        // Validate the whole proposed aggregate before changing any managed entity.
        for (var edit : edits) {
            RoadmapTask task = byId.get(edit.id());
            if (edit.state() != task.getState() && (edit.state() == TaskState.IN_PROGRESS || edit.state() == TaskState.COMPLETED)
                    && !ready(task, proposed)) throw new ConflictException("Complete the missing prerequisite tasks before starting or completing this task. Skipping a foundation does not satisfy it.");
        }
        if (edits.isEmpty() && request.title() == null) return describe(roadmap);
        if (request.title() != null) roadmap.setTitle(request.title().strip());
        for (var edit : edits) byId.get(edit.id()).update(edit.title().strip(), edit.estimatedHours(), edit.state());
        roadmap.touch();
        roadmaps.flush();
        return describe(roadmap);
    }

    private Roadmap requireOwned(Authentication authentication, UUID id) {
        return roadmaps.findByIdAndUserId(id, auth.requireUser(authentication).getId())
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap was not found."));
    }
    private List<RoadmapTask> tasks(Roadmap roadmap) {
        return roadmap.getPhases().stream().flatMap(phase -> phase.getTasks().stream()).toList();
    }
    private Map<UUID, TaskState> states(List<RoadmapTask> tasks) {
        return tasks.stream().collect(Collectors.toMap(RoadmapTask::getId, RoadmapTask::getState));
    }
    private boolean ready(RoadmapTask task, Map<UUID, TaskState> states) {
        return task.getPrerequisites().entrySet().stream()
                .allMatch(entry -> entry.getValue() || states.get(entry.getKey()) == TaskState.COMPLETED);
    }

    private RoadmapResponse describe(Roadmap roadmap) {
        List<RoadmapTask> all = tasks(roadmap);
        Map<UUID, TaskState> states = states(all);
        Map<UUID, RoadmapTask> byId = all.stream().collect(Collectors.toMap(RoadmapTask::getId, Function.identity()));
        Map<UUID, RoadmapResponse.Task> responses = new HashMap<>();
        for (RoadmapTask task : all) {
            var prerequisites = task.getPrerequisites().entrySet().stream().map(entry -> new RoadmapResponse.Prerequisite(
                    entry.getKey(), byId.get(entry.getKey()).getSkillName(), entry.getValue(),
                    entry.getValue() || states.get(entry.getKey()) == TaskState.COMPLETED))
                    .sorted(Comparator.comparing(RoadmapResponse.Prerequisite::skillName).thenComparing(RoadmapResponse.Prerequisite::taskId)).toList();
            responses.put(task.getId(), new RoadmapResponse.Task(task.getId(), task.getSkillId(), task.getSkillName(),
                    task.getTitle(), task.getState(), task.getTargetProficiency(), task.getEstimatedHours(),
                    task.isSatisfiedAtGeneration(), ready(task, states), task.getInitialPriority(), task.getPriorityPoints(),
                    task.getOrderingReason(), prerequisites));
        }
        var phases = roadmap.getPhases().stream().map(phase -> new RoadmapResponse.Phase(phase.getId(), phase.getPosition(),
                phase.getTitle(), phase.getTasks().stream().map(task -> responses.get(task.getId())).toList())).toList();
        UUID currentPhase = phases.stream().filter(phase -> phase.tasks().stream().anyMatch(task -> !task.state().terminal()))
                .map(RoadmapResponse.Phase::id).findFirst().orElse(null);
        var eligible = all.stream().filter(task -> !task.getState().terminal() && ready(task, states))
                .sorted(Comparator.comparingInt(task -> task.getState() == TaskState.IN_PROGRESS ? 0 : 1)).toList();
        List<RoadmapResponse.WeeklyFocus> week = new ArrayList<>();
        int remaining = roadmap.getWeeklyHours();
        for (RoadmapTask task : eligible) {
            if (remaining <= 0 || week.size() >= policy.focusSlots(roadmap.getWeeklyHours())) break;
            int allocation = Math.min(remaining, task.getEstimatedHours());
            week.add(new RoadmapResponse.WeeklyFocus(task.getId(), task.getTitle(), allocation));
            remaining -= allocation;
        }
        var next = week.isEmpty() ? null : responses.get(week.getFirst().taskId());
        return new RoadmapResponse(roadmap.getId(), roadmap.getCareerId(), roadmap.getCareerName(), roadmap.getTitle(),
                roadmap.getRevision(), roadmap.getGenerationVersion(), roadmap.getDecisionVersion(), "DEMO DATA",
                roadmap.getCreatedAt(), roadmap.getUpdatedAt(), roadmap.getProfileUpdatedAt(), roadmap.getPreviousRoadmapId(),
                roadmap.getWeeklyHours(), currentPhase, next == null ? "NOT_YET" : "LEARN_NOW", next, List.copyOf(week), phases);
    }
}
