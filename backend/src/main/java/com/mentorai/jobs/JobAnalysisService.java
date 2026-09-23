package com.mentorai.jobs;

import com.mentorai.auth.service.AuthService;
import com.mentorai.profile.service.ProfileService;
import com.mentorai.decision.service.LearningPriorityPolicy;
import com.mentorai.jobs.JobModels.*;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobAnalysisService {
    private final AuthService auth;
    private final ProfileService profiles;
    private final JobExtractionService extraction;
    private final JobMatchingService matching;
    private final JobAnalysisRepository repository;
    public JobAnalysisService(AuthService auth,ProfileService profiles,JobExtractionService extraction,JobMatchingService matching,JobAnalysisRepository repository) {
        this.auth=auth;this.profiles=profiles;this.extraction=extraction;this.matching=matching;this.repository=repository;
    }
    @Transactional
    public Analysis create(AnalysisRequest request,Authentication authentication) {
        var user=auth.requireUser(authentication);
        var profile=profiles.get(authentication);
        var known=profile.skills().stream().collect(Collectors.toMap(s->s.id(),s->s.proficiency()));
        var matches=matching.match(request,known);
        Integer indicator=matching.indicator(matches);
        String status=indicator==null?"INSUFFICIENT_REQUIREMENTS":matches.stream().anyMatch(s->s.status().equals("UNASSESSED"))?"PARTIAL_ANALYSIS":"CATALOG_SKILLS_ASSESSED";
        var result=new Analysis(UUID.randomUUID(),Instant.now(),JobMatchingService.VERSION,extraction.extract(request.description()),request,
                new ProfileInputs(profile.updatedAt(),profile.timeAvailablePerWeek(),profile.skills()),status,indicator,matches,
                matching.priorities(matches,known,profile.timeAvailablePerWeek()),LearningPriorityPolicy.VERSION,
                "Catalog skill coverage only, not hiring probability or full job readiness. Required weight=3; preferred=1. Missing=0, Awareness=1/3, Beginner=2/3, Intermediate/Advanced=1. Round 100*weighted coverage/total assessed weight; no score without assessed skills. Intermediate is an illustrative comparison target, not employer-required proficiency. Unknown and unclassified skills, experience, responsibilities and location are unassessed. Preparation uses the DEMO prerequisite graph and existing time/readiness gates, without market evidence. Reviewed requirements are supplied by you; listing authenticity is not verified.");
        repository.save(user.getId(),result);
        return result;
    }
    @Transactional(readOnly=true)
    public Analysis get(UUID id,Authentication authentication) { return repository.get(id,auth.requireUser(authentication).getId()); }
}
