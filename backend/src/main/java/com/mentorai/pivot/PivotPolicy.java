package com.mentorai.pivot;

import com.mentorai.decision.dto.*;
import com.mentorai.profile.dto.ProfileResponse;
import com.mentorai.roadmap.dto.RoadmapResponse;
import com.mentorai.roadmap.entity.TaskState;
import com.mentorai.skills.entity.SkillProficiency;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import static com.mentorai.pivot.PivotModels.*;

@Component
public class PivotPolicy {
    public static final String VERSION="career-pivot-v1";
    public List<Credit> credits(RoadmapResponse source, Map<UUID,com.mentorai.roadmap.repository.RoadmapCreditRepository.Evidence> evidence) {
        return source.phases().stream().flatMap(p->p.tasks().stream())
                .filter(t->t.state()==TaskState.COMPLETED || t.state()==TaskState.SKIPPED && t.satisfiedAtGeneration()
                        && (!evidence.containsKey(t.id()) || !evidence.get(t.id()).revoked()))
                .map(t->new Credit(t.id(),t.skillId(),t.skillName(),t.state()==TaskState.COMPLETED || !evidence.containsKey(t.id()) ? t.targetProficiency() : evidence.get(t.id()).proficiency(),
                        t.state()==TaskState.COMPLETED?"COMPLETED_TASK":"RETAINED_GENERATION_CREDIT")).toList();
    }
    public Map<UUID,SkillProficiency> effective(ProfileResponse profile,List<Credit> credits) {
        Map<UUID,SkillProficiency> known=new HashMap<>();
        profile.skills().forEach(s->known.put(s.id(),s.proficiency()));
        credits.forEach(c->known.merge(c.skillId(),c.proficiency(),(a,b)->a.ordinal()>=b.ordinal()?a:b));
        return known;
    }
    public Comparison compare(RoadmapResponse source, ProfileResponse profile, List<Credit> credits,
                              LearningPrioritiesResponse before, LearningPrioritiesResponse after, List<Stage> stages) {
        var old=before.decisions().stream().collect(Collectors.toMap(LearningDecision::skillId,Function.identity()));
        var next=after.decisions().stream().collect(Collectors.toMap(LearningDecision::skillId,Function.identity()));
        List<Skill> transferable=new ArrayList<>(), newlyRequired=new ArrayList<>(), skipped=new ArrayList<>();
        Map<UUID,Skill> satisfied=new TreeMap<>();
        for(var d:after.decisions()) {
            Skill skill=new Skill(d.skillId(),d.name(),d.currentProficiency(),d.targetProficiency());
            if(d.currentProficiency()!=null)transferable.add(skill);
            if(d.careerRelevance().startsWith("REQUIRED") && (!old.containsKey(d.skillId()) || !old.get(d.skillId()).careerRelevance().startsWith("REQUIRED")))newlyRequired.add(skill);
            if(d.reasonCodes().contains("ALREADY_PROFICIENT"))skipped.add(skill);
            for(var p:d.prerequisiteReadiness().prerequisites())if(p.satisfied())
                satisfied.put(p.skillId(),new Skill(p.skillId(),p.name(),p.currentProficiency(),SkillProficiency.BEGINNER));
        }
        Set<UUID> ids=new TreeSet<>(old.keySet());ids.addAll(next.keySet());
        List<PriorityChange> changes=new ArrayList<>();
        for(UUID id:ids) {
            var a=old.get(id);var b=next.get(id);
            if(a==null||b==null||a.priority()!=b.priority()||a.deterministicScore()!=b.deterministicScore()||!a.careerRelevance().equals(b.careerRelevance()))
                changes.add(new PriorityChange(id,b==null?a.name():b.name(),a==null?"NOT_IN_CAREER":a.priority().name(),b==null?"NOT_IN_CAREER":b.priority().name(),a==null?null:a.deterministicScore(),b==null?null:b.deterministicScore(),a==null?null:a.careerRelevance(),b==null?null:b.careerRelevance()));
        }
        int hours=stages.stream().flatMap(s->s.tasks().stream()).mapToInt(ProposedTask::estimatedHours).sum();
        String band=hours==0?"TARGETS_MET":hours<=16?"SMALL":hours<=40?"MODERATE":"SUBSTANTIAL";
        Effort effort=new Effort(band,hours,(hours+after.weeklyHours()-1)/after.weeklyHours(),
                "Illustrative 0/4/8-hour task estimates, not validated mastery times. Capacity-weeks divide total effort by recorded weekly hours; prerequisites, interruptions and existing commitments may extend elapsed time. No hiring-readiness claim.");
        return new Comparison(VERSION,source,profile.updatedAt(),before,after,credits,List.copyOf(transferable),List.copyOf(newlyRequired),List.copyOf(satisfied.values()),List.copyOf(skipped),List.copyOf(changes),effort,stages);
    }
}
