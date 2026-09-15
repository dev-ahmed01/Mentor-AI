package com.mentorai.skills.service;

import com.mentorai.career.repository.CareerRepository;
import com.mentorai.common.exception.ResourceNotFoundException;
import com.mentorai.profile.dto.ProfileResponse;
import com.mentorai.profile.service.ProfileService;
import com.mentorai.skills.dto.SkillDependencyResponse;
import com.mentorai.skills.dto.SkillPrerequisitesResponse;
import com.mentorai.skills.dto.SkillPrerequisitesResponse.Prerequisite;
import com.mentorai.skills.entity.Skill;
import com.mentorai.skills.entity.SkillDependency;
import com.mentorai.skills.entity.SkillProficiency;
import com.mentorai.skills.repository.SkillDependencyRepository;
import com.mentorai.skills.repository.SkillRepository;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SkillDependencyService {
    private final SkillRepository skills;
    private final SkillDependencyRepository dependencies;
    private final ProfileService profiles;
    private final CareerRepository careers;

    public SkillDependencyService(SkillRepository skills, SkillDependencyRepository dependencies,
                                  ProfileService profiles, CareerRepository careers) {
        this.skills = skills;
        this.dependencies = dependencies;
        this.profiles = profiles;
        this.careers = careers;
    }

    public void validateGraph() { loadGraph(); }

    public List<SkillDependencyResponse> dependencies(UUID id) {
        requireSkill(id);
        GraphData graph = loadGraph();
        return graph.edges().stream().filter(edge -> edge.getSkill().getId().equals(id))
                .map(edge -> new SkillDependencyResponse(id, edge.getPrerequisite().getId(),
                        edge.getPrerequisite().getName(), edge.getImportance(), "DEMO DATA"))
                .toList();
    }

    public SkillPrerequisitesResponse prerequisites(UUID id, Authentication authentication) {
        Skill target = requireSkill(id);
        return readiness(target, loadGraph(), proficiencies(profiles.get(authentication)));
    }

    public List<SkillPrerequisitesResponse> forCareer(UUID careerId, Authentication authentication) {
        var career = careers.findByIdAndActiveTrue(careerId)
                .orElseThrow(() -> new ResourceNotFoundException("Career was not found."));
        GraphData graph = loadGraph();
        Map<UUID, SkillProficiency> known = proficiencies(profiles.get(authentication));
        return career.getSkills().stream().map(item -> item.getSkill())
                .sorted(Comparator.comparing(Skill::getName))
                .map(skill -> readiness(skill, graph, known)).toList();
    }

    private SkillPrerequisitesResponse readiness(
            Skill target, GraphData graph, Map<UUID, SkillProficiency> known) {
        Set<UUID> direct = graph.edges().stream()
                .filter(edge -> edge.getSkill().getId().equals(target.getId()))
                .map(edge -> edge.getPrerequisite().getId()).collect(Collectors.toSet());
        List<Prerequisite> requirements = graph.graph().prerequisites(target.getId()).stream()
                .map(id -> new Prerequisite(id, graph.names().get(id), direct.contains(id), known.get(id),
                        known.containsKey(id) && known.get(id) != SkillProficiency.AWARENESS))
                .sorted(Comparator.comparing(Prerequisite::name).thenComparing(Prerequisite::skillId))
                .toList();
        return new SkillPrerequisitesResponse(target.getId(), target.getName(),
                requirements.stream().allMatch(Prerequisite::satisfied),
                requirements.isEmpty() ? "NO_RECORDED_PREREQUISITES" : "MODELED_PREREQUISITES",
                SkillProficiency.BEGINNER, "skill-prerequisites-v1", "DEMO DATA", requirements);
    }

    private Map<UUID, SkillProficiency> proficiencies(ProfileResponse profile) {
        return profile.skills().stream().collect(Collectors.toMap(item -> item.id(), item -> item.proficiency()));
    }

    private Skill requireSkill(UUID id) {
        return skills.findById(id).orElseThrow(() -> new ResourceNotFoundException("Skill was not found."));
    }

    private GraphData loadGraph() {
        List<SkillDependency> edges = dependencies.findGraph();
        Map<UUID, String> names = new HashMap<>();
        edges.forEach(edge -> names.put(edge.getPrerequisite().getId(), edge.getPrerequisite().getName()));
        var graph = new SkillDependencyGraph(edges.stream().map(edge -> new SkillDependencyGraph.Edge(
                edge.getSkill().getId(), edge.getPrerequisite().getId())).toList());
        return new GraphData(graph, edges, names);
    }

    private record GraphData(SkillDependencyGraph graph, List<SkillDependency> edges, Map<UUID, String> names) { }
}
