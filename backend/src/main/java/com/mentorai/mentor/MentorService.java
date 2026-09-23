package com.mentorai.mentor;

import com.mentorai.auth.service.AuthService;
import com.mentorai.career.repository.CareerRepository;
import com.mentorai.jobs.JobAnalysisService;
import com.mentorai.common.exception.*;
import com.mentorai.mentor.MentorModels.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class MentorService {
    public static final String PROMPT_VERSION="mentor-v1";
    private final AuthService auth;private final CareerRepository careers;private final JobAnalysisService jobs;private final MentorRepository repository;
    private final MentorContextService contexts;private final AiProvider provider;private final MentorOutputValidator validator;private final String policy;
    public MentorService(AuthService auth,CareerRepository careers,JobAnalysisService jobs,MentorRepository repository,MentorContextService contexts,AiProvider provider,MentorOutputValidator validator)throws IOException{
        this.auth=auth;this.careers=careers;this.jobs=jobs;this.repository=repository;this.contexts=contexts;this.provider=provider;this.validator=validator;
        policy=new ClassPathResource("prompts/mentor-v1.txt").getContentAsString(StandardCharsets.UTF_8);
    }
    public Map<String,Object> status(){return Map.of("enabled",provider.enabled(),"model",provider.model(),"mode","EVIDENCE_SELECTION","promptVersion",PROMPT_VERSION);}
    public List<Conversation> list(Authentication authentication){return repository.list(auth.requireUser(authentication).getId());}
    public Conversation create(CreateRequest request,Authentication authentication){
        UUID owner=auth.requireUser(authentication).getId();var career=careers.findByIdAndActiveTrue(request.careerId()).orElseThrow(()->new ResourceNotFoundException("Career was not found."));
        if(request.jobAnalysisId()!=null)jobs.get(request.jobAnalysisId(),authentication);
        Instant now=Instant.now();var c=new Conversation(UUID.randomUUID(),career.getId(),career.getName(),request.jobAnalysisId(),now,now,0,"");repository.create(c,owner);return c;
    }
    public History history(UUID id,int page,Authentication authentication){
        if(page<0||page>2)throw new com.mentorai.roadmap.service.RoadmapValidationException("History page must be between 0 and 2.");
        var c=repository.get(id,auth.requireUser(authentication).getId());var turns=new ArrayList<>(repository.turns(id,20,page*20));Collections.reverse(turns);
        return new History(c,List.copyOf(turns),page,c.revision()>(page+1)*20L);
    }
    public Turn message(UUID id,MessageRequest request,Authentication authentication){
        UUID owner=auth.requireUser(authentication).getId();var c=repository.get(id,owner);var previous=repository.request(id,request.requestId());
        if(previous.isPresent()){if(!previous.get().question().equals(request.question())||previous.get().revision()!=request.expectedRevision()+1)throw new ConflictException("Request ID was already used for another message.");return previous.get();}
        if(c.revision()!=request.expectedRevision()||c.revision()>=60)throw new ConflictException("Conversation changed or reached its 60-message limit. Reload or start another conversation.");
        var context=contexts.build(c,request.question(),repository.turns(id,4,0),authentication);
        String state="UNAVAILABLE",answer="AI mentor is currently unavailable. Your message was saved; deterministic learning tools remain available.";
        List<Fact> citations=List.of();NextStep next=null;
        try{
            String data=repository.encode(Map.of("question",request.question(),"context",context));
            if(data.length()>40000)throw new IllegalStateException("Context limit exceeded.");
            String output=provider.generate(policy,data);
            Selection selected;
            try{selected=validator.validate(output,context);}catch(IllegalArgumentException invalid){state="INVALID_OUTPUT";throw invalid;}
            citations=selected.factIds().stream().map(key->context.facts().stream().filter(f->f.id().equals(key)).findFirst().orElseThrow()).toList();
            answer="Here is what your recorded information supports:\n\n"+String.join("\n\n",citations.stream().map(Fact::text).toList());
            next=nextStep(selected.nextStep(),c);state="ANSWERED";
        }catch(Exception ignored){/* No model text, prompt, credentials or transport errors are returned or logged. */}
        var turn=new Turn(UUID.randomUUID(),request.requestId(),c.revision()+1,Instant.now(),request.question(),state,answer,context.marketNotice(),citations,next,context,PROMPT_VERSION,provider.model());
        String memory=memory(c.memory(),request.question());
        try{return repository.append(c,owner,turn,memory);}catch(ConflictException conflict){var duplicate=repository.request(id,request.requestId());if(duplicate.isPresent()&&duplicate.get().question().equals(request.question())&&duplicate.get().revision()==request.expectedRevision()+1)return duplicate.get();throw conflict;}
    }
    private NextStep nextStep(String code,Conversation c){return switch(code){
        case "REVIEW_CHECK_IN"->new NextStep(code,"Review capacity and temporary constraints in Weekly check-in","/progress");
        case "REVIEW_PROFILE"->new NextStep(code,"Review your recorded skills and weekly availability","/profile");
        case "REVIEW_MARKET"->new NextStep(code,"Inspect source evidence and its limitations","/market?careerId="+c.careerId());
        case "REVIEW_JOB"->new NextStep(code,"Inspect the saved job comparison","/jobs/analyses/"+c.jobAnalysisId());
        default->new NextStep(code,"Inspect deterministic learning priorities","/dashboard?careerId="+c.careerId());};}
    static String memory(String previous,String question){
        Set<String> topics=new TreeSet<>();if(!previous.isBlank())topics.addAll(Arrays.asList(previous.split(", ")));
        String q=question.toLowerCase(Locale.ROOT);topics.add(q.matches("(?s).*(exam|week|time|schedule|capacity).*")?"capacity and check-ins":q.matches("(?s).*(job|interview|resume).*")?"job preparation":q.matches("(?s).*(market|demand|salary).*")?"market evidence":"learning priorities");
        return String.join(", ",topics);
    }
}
