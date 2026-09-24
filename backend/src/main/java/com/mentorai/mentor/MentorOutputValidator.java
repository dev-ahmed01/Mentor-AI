package com.mentorai.mentor;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.mentorai.mentor.MentorModels.*;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class MentorOutputValidator {
    private final ObjectMapper json;
    public MentorOutputValidator(ObjectMapper json){this.json=json;}
    public Selection validate(String output,Context context){
        if(output==null||output.length()>8192)throw new IllegalArgumentException("Invalid AI response.");
        try{
            var root=json.reader().with(JsonParser.Feature.STRICT_DUPLICATE_DETECTION).with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).readTree(output);
            if(!root.isObject()||root.size()!=2||!root.has("factIds")||!root.has("nextStep")||!root.path("factIds").isArray()
                    ||root.path("factIds").size()<1||root.path("factIds").size()>6||!root.path("nextStep").isTextual())throw new IllegalArgumentException();
            List<String> ids=new ArrayList<>();
            for(var id:root.path("factIds")){if(!id.isTextual()||context.facts().stream().noneMatch(f->f.id().equals(id.asText()))||ids.contains(id.asText()))throw new IllegalArgumentException();ids.add(id.asText());}
            String next=root.path("nextStep").asText();
            if(!Set.of("REVIEW_PRIORITIES","REVIEW_CHECK_IN","REVIEW_PROFILE","REVIEW_MARKET","REVIEW_JOB").contains(next)
                    ||(next.equals("REVIEW_JOB")&&context.facts().stream().noneMatch(f->f.id().equals("job"))))throw new IllegalArgumentException();
            return new Selection(List.copyOf(ids),next);
        }catch(Exception e){throw new IllegalArgumentException("Invalid AI response.");}
    }
}
