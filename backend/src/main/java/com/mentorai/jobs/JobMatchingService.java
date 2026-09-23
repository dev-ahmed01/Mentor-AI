package com.mentorai.jobs;

import com.mentorai.jobs.JobModels.*;
import com.mentorai.skills.entity.Skill;
import com.mentorai.skills.entity.SkillProficiency;
import com.mentorai.skills.dto.SkillPrerequisitesResponse;
import com.mentorai.skills.service.SkillDependencyService;
import com.mentorai.decision.dto.LearningDecision;
import com.mentorai.decision.service.LearningPriorityPolicy;
import com.mentorai.decision.service.LearningPriorityPolicy.Candidate;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class JobMatchingService {
    public static final String VERSION="job-match-v1";
    private final JobSkillVocabulary vocabulary;
    private final SkillDependencyService dependencies;
    private final LearningPriorityPolicy policy;
    public JobMatchingService(JobSkillVocabulary vocabulary,SkillDependencyService dependencies,LearningPriorityPolicy policy) {
        this.vocabulary=vocabulary;this.dependencies=dependencies;this.policy=policy;
    }
    public List<SkillMatch> match(AnalysisRequest job,Map<UUID,SkillProficiency> known) {
        List<Skill> catalog=vocabulary.all();
        Map<String,SkillMatch> unique=new LinkedHashMap<>();
        add(unique,job.requiredSkills(),"REQUIRED",catalog,known);
        add(unique,job.preferredSkills(),"PREFERRED",catalog,known);
        add(unique,job.unclassifiedSkills(),"UNCLASSIFIED",catalog,known);
        return unique.values().stream().sorted(Comparator.comparing(SkillMatch::name)).toList();
    }
    private void add(Map<String,SkillMatch> unique,List<String> names,String requirement,List<Skill> catalog,Map<UUID,SkillProficiency> known) {
        for(String input:names) {
            Skill skill=vocabulary.resolve(input,catalog).orElse(null);
            String key=skill==null?"name:"+JobSkillVocabulary.key(input):"id:"+skill.getId();
            var current=skill==null?null:known.get(skill.getId());
            boolean assessed=skill!=null&&!requirement.equals("UNCLASSIFIED");
            String status=!assessed?"UNASSESSED":current==null?"MISSING":met(current,SkillProficiency.INTERMEDIATE)?"MATCHED":"PARTIAL";
            unique.putIfAbsent(key,new SkillMatch(skill==null?null:skill.getId(),skill==null?input.strip():skill.getName(),requirement,current,
                    assessed?SkillProficiency.INTERMEDIATE:null,status,assessed?(requirement.equals("REQUIRED")?3:1):0));
        }
    }
    public Integer indicator(List<SkillMatch> skills) {
        int total=skills.stream().mapToInt(SkillMatch::weight).sum();
        if(total==0)return null;
        double achieved=skills.stream().mapToDouble(s->s.weight()*(s.currentProficiency()==null?0:Math.min(1,(s.currentProficiency().ordinal()+1)/3.0))).sum();
        return (int)Math.round(100*achieved/total);
    }
    public List<LearningDecision> priorities(List<SkillMatch> matches,Map<UUID,SkillProficiency> known,Integer hours) {
        var direct=matches.stream().filter(s->s.weight()>0).collect(Collectors.toMap(SkillMatch::skillId,s->s));
        Map<UUID,SkillPrerequisitesResponse> readiness=new HashMap<>();
        for(UUID id:direct.keySet())readiness.put(id,dependencies.prerequisites(id,known));
        var ancestors=readiness.values().stream().flatMap(r->r.prerequisites().stream()).map(SkillPrerequisitesResponse.Prerequisite::skillId).distinct().toList();
        for(UUID id:ancestors)if(!readiness.containsKey(id))readiness.put(id,dependencies.prerequisites(id,known));
        List<Candidate> candidates=new ArrayList<>();
        for(var item:readiness.values()) {
            var own=direct.get(item.skillId());
            var dependents=direct.values().stream().filter(target->readiness.get(target.skillId()).prerequisites().stream().anyMatch(p->p.skillId().equals(item.skillId()))).toList();
            var unmet=dependents.stream().filter(d->!met(known.get(d.skillId()),SkillProficiency.INTERMEDIATE)).toList();
            int bottlenecks=met(known.get(item.skillId()),SkillProficiency.BEGINNER)?0:unmet.size();
            boolean requiredFoundation=(own==null?dependents:unmet).stream().anyMatch(d->d.requirement().equals("REQUIRED"));
            String relevance=own!=null&&own.requirement().equals("REQUIRED")?"REQUIRED"
                    :requiredFoundation&&(own==null||bottlenecks>0)?"REQUIRED_FOUNDATION":own!=null?"PREFERRED":"PREFERRED_FOUNDATION";
            candidates.add(new Candidate(item,known.get(item.skillId()),own==null?SkillProficiency.BEGINNER:SkillProficiency.INTERMEDIATE,relevance,1,bottlenecks));
        }
        return policy.decide(candidates,hours).stream().map(d->new LearningDecision(d.skillId(),d.name(),d.priority(),d.deterministicScore(),
                d.prerequisiteReadiness(),d.careerRelevance(),d.importance(),d.currentProficiency(),d.targetProficiency(),d.learningDistance(),
                d.estimatedEffortBand(),d.bottleneckCount(),d.reasonCodes().stream().map(r->r.replace("TARGET_CAREER_","TARGET_JOB_")).toList(),d.evidenceStatus())).toList();
    }
    private static boolean met(SkillProficiency current,SkillProficiency target) { return current!=null&&current.ordinal()>=target.ordinal(); }
}
