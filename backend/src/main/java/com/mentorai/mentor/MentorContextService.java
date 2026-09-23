package com.mentorai.mentor;

import com.mentorai.mentor.MentorModels.*;
import com.mentorai.profile.service.ProfileService;
import com.mentorai.decision.service.LearningDecisionService;
import com.mentorai.roadmap.service.RoadmapService;
import com.mentorai.progress.service.WeeklyProgressService;
import com.mentorai.market.MarketRepository;
import com.mentorai.market.MarketAnalyticsService;
import com.mentorai.jobs.JobAnalysisService;
import com.mentorai.jobs.JobSkillVocabulary;
import com.mentorai.common.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class MentorContextService {
    public static final String NO_MARKET="Insufficient market evidence available.";
    private final ProfileService profiles;private final LearningDecisionService decisions;private final RoadmapService roadmaps;
    private final WeeklyProgressService progress;private final MarketRepository market;private final MarketAnalyticsService analytics;
    private final JobAnalysisService jobs;private final JobSkillVocabulary vocabulary;
    public MentorContextService(ProfileService profiles,LearningDecisionService decisions,RoadmapService roadmaps,WeeklyProgressService progress,
                                MarketRepository market,MarketAnalyticsService analytics,JobAnalysisService jobs,JobSkillVocabulary vocabulary){
        this.profiles=profiles;this.decisions=decisions;this.roadmaps=roadmaps;this.progress=progress;this.market=market;this.analytics=analytics;this.jobs=jobs;this.vocabulary=vocabulary;
    }
    public Context build(Conversation conversation,String question,List<Turn> recent,Authentication auth){
        var profile=profiles.get(auth);
        var known=profile.skills().stream().collect(java.util.stream.Collectors.toMap(s->s.id(),s->s.proficiency()));
        var priorities=decisions.priorities(conversation.careerId(),known,profile.timeAvailablePerWeek());List<Fact> facts=new ArrayList<>();
        String careerLink="/dashboard?careerId="+conversation.careerId();
        facts.add(fact("profile","RECORDED","Recorded weekly learning time: "+(profile.timeAvailablePerWeek()==null?"not provided":profile.timeAvailablePerWeek()+" hours")+". Current focus limit: "+priorities.immediateFocusLimit()+" skills. This is a planning limit, not a completion promise.","/profile",Map.of("profileUpdatedAt",profile.updatedAt().toString())));
        var mentioned=vocabulary.all().stream().filter(s->vocabulary.mentioned(s,question)).map(s->s.getId()).toList();
        var selected=priorities.decisions().stream().sorted(Comparator.comparingInt(d->mentioned.contains(d.skillId())?0:1)).limit(12).toList();
        for(var d:selected){
            String gaps=String.join(", ",d.prerequisiteReadiness().prerequisites().stream().filter(p->!p.satisfied()).map(p->p.name()).toList());
            String explanation=d.reasonCodes().contains("ALREADY_PROFICIENT")?"the comparison target is already recorded as met"
                    :!d.prerequisiteReadiness().eligible()?"build these prerequisites first: "+gaps
                    :"prerequisites are met; the priority also respects your weekly focus limit";
            facts.add(fact("priority:"+d.skillId(),"CALCULATED",d.name()+": "+(d.reasonCodes().contains("ALREADY_PROFICIENT")?"target met":d.priority().name().replace("_"," "))+" because "+explanation+". Recorded level: "+(d.currentProficiency()==null?"not recorded":d.currentProficiency())+"; comparison target: "+d.targetProficiency()+". Ordering score: "+d.deterministicScore()+", not hiring probability. Catalog prerequisites are DEMO DATA.",careerLink,Map.of("policy",priorities.calculationVersion(),"skillId",d.skillId().toString())));
        }
        if(mentioned.stream().anyMatch(id->priorities.decisions().stream().noneMatch(d->d.skillId().equals(id))))
            facts.add(fact("outside-career","LIMITATION","A mentioned skill is outside this conversation's selected career requirements. No priority for that skill is inferred; choose the relevant career to inspect its prerequisites.","/careers",Map.of()));
        try{
            var roadmap=roadmaps.current(auth);
            if(roadmap.careerId().equals(conversation.careerId())){
                var phase=roadmap.phases().stream().filter(p->p.id().equals(roadmap.currentPhaseId())).findFirst();
                long done=roadmap.phases().stream().flatMap(p->p.tasks().stream()).filter(t->t.state().name().equals("COMPLETED")).count();
                facts.add(fact("roadmap","RECORDED","Current roadmap phase: "+phase.map(p->p.title()).orElse("no current phase")+". Completed tasks: "+done+". "+(roadmap.nextAction()==null?"No next task is assigned.":"Next recorded task: "+roadmap.nextAction().title()+"."),"/roadmap",Map.of("roadmapId",roadmap.id().toString(),"revision",Long.toString(roadmap.revision()))));
                try{var week=progress.currentPlan(auth,null);if(week.roadmapId().equals(roadmap.id()))facts.add(fact("weekly","RECORDED","Week of "+week.weekStart()+": "+week.plannedHours()+" hours planned against "+week.capacityHours()+" hours capacity. Mode: "+week.mode()+".","/progress",Map.of("weeklyPlanId",week.id().toString())));}catch(ResourceNotFoundException ignored){/* Explicit absence is supported. */}
                try{var check=progress.currentCheckIn(auth,null);if(check.roadmapId().equals(roadmap.id()))facts.add(fact("check-in","RECORDED","Latest check-in for this week records "+check.actualHours()+" actual hours and "+check.availableHoursNextWeek()+" hours available next week. "+(check.constraint()==null?"No temporary constraint recorded.":"Temporary constraint: "+check.constraint().type()+" from "+check.constraint().startDate()+" through "+check.constraint().endDate()+"."),"/progress",Map.of("checkInId",check.id().toString())));}catch(ResourceNotFoundException ignored){/* Do not send unbounded check-in history. */}
            }
        }catch(ResourceNotFoundException ignored){facts.add(fact("roadmap-missing","LIMITATION","No saved roadmap is available yet. Inspect learning priorities before creating a plan.","/roadmap",Map.of()));}
        String marketNotice=NO_MARKET;
        var snapshot=market.latest(conversation.careerId());
        if(snapshot.isPresent()){
            var s=snapshot.get();var evidence=analytics.evidence(s,Instant.now());
            if(evidence.status().equals("AVAILABLE")){
                marketNotice="Market observations are a bounded single-source sample, not global demand or hiring probability.";
                facts.add(fact("market","OBSERVED","Arbeitnow sample: "+s.sampleSize()+" listings from "+s.employerCount()+" employers, collected "+s.collectedAt()+" for publication window "+s.windowStart()+" to "+s.windowEnd()+". "+s.sourceContext(),"/market?careerId="+conversation.careerId(),Map.of("snapshotId",s.id().toString(),"source",s.source(),"collectedAt",s.collectedAt().toString(),"windowStart",s.windowStart().toString(),"windowEnd",s.windowEnd().toString(),"sampleSize",Integer.toString(s.sampleSize()),"processingVersion",s.processingVersion())));
            }else facts.add(fact("market-unavailable","LIMITATION",NO_MARKET+" Evidence status: "+evidence.status()+". "+evidence.message(),"/market?careerId="+conversation.careerId(),Map.of("snapshotId",s.id().toString())));
        }else facts.add(fact("market-unavailable","LIMITATION",NO_MARKET,"/market?careerId="+conversation.careerId(),Map.of()));
        if(conversation.jobAnalysisId()!=null){
            var job=jobs.get(conversation.jobAnalysisId(),auth);
            facts.add(fact("job","CALCULATED","Saved job comparison: "+(job.matchIndicator()==null?"no scored requirements":job.matchIndicator()+" / 100 catalog skill coverage")+". Status: "+job.status()+". This is historical, not hiring probability or full job readiness; experience and location are not assessed.","/jobs/analyses/"+job.id(),Map.of("jobAnalysisId",job.id().toString(),"calculatedAt",job.calculatedAt().toString(),"policy",job.calculationVersion())));
        }
        facts.add(fact("constraints","RECOMMENDATION","If exams or other constraints reduce your capacity, record dates and available hours in Weekly check-in. The deterministic planner can propose a change; review it before accepting. This conversation does not change any plan.","/progress",Map.of()));
        return new Context(Instant.now(),conversation.careerId(),conversation.careerName(),List.copyOf(facts),marketNotice,
                recent.stream().limit(4).map(t->new Recent(t.question().substring(0,Math.min(500,t.question().length())),t.citations().stream().map(Fact::id).toList(),t.status())).toList(),conversation.memory());
    }
    private Fact fact(String id,String kind,String text,String href,Map<String,String> provenance){return new Fact(id,kind,text,href,provenance);}
}
