package com.mentorai.jobs;

import com.mentorai.jobs.JobModels.Draft;
import java.util.*;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional(readOnly=true)
public class JobExtractionService {
    public static final String VERSION="job-extraction-v1";
    private static final Pattern REQUIRED=Pattern.compile("(?i)\\b(required|mandatory|must have|must-have)\\b");
    private static final Pattern PREFERRED=Pattern.compile("(?i)\\b(preferred|optional|nice to have|nice-to-have|desirable)\\b");
    private static final Pattern NEGATION=Pattern.compile("(?iu)\\b(not|no|never|without|[a-z]+n['’]t)\\b");
    private final JobSkillVocabulary vocabulary;
    public JobExtractionService(JobSkillVocabulary vocabulary) { this.vocabulary=vocabulary; }

    public Draft extract(String description) {
        var catalog=vocabulary.all();
        Map<String,String> requirements=new TreeMap<>();
        Map<String,StringBuilder> metadata=new HashMap<>();
        String section="UNCLASSIFIED";
        // A preview is heuristic. The full original text remains available for explicit review.
        String plain=description.replaceAll("(?is)<(script|style)[^>]*>.*?</\\1>"," ")
                .replaceAll("(?i)</(?:p|li|h[1-6])>|<br\\s*/?>","\n").replaceAll("<[^>]*>"," ");
        for(String rawLine:plain.split("\\R")) {
            String line=rawLine.strip().replaceFirst("^[#*\\-•]+\\s*","");
            int colon=line.indexOf(':');
            String label=(colon>=0?line.substring(0,colon):line).strip().toLowerCase(Locale.ROOT);
            String heading=switch(label) {
                case "required","required skills","requirements","must have","must-have","essential skills" -> "REQUIRED";
                case "preferred","preferred skills","nice to have","nice-to-have","desirable skills" -> "PREFERRED";
                case "title","job title" -> "title";
                case "responsibilities" -> "responsibilities";
                case "experience","experience expectations" -> "experience";
                case "location" -> "location";
                case "technologies","technology stack" -> "technologies";
                default -> null;
            };
            if(heading!=null) { section=heading;line=colon>=0?line.substring(colon+1).strip():""; }
            else if(colon>=0)section="UNCLASSIFIED";
            if(Set.of("title","responsibilities","experience","location","technologies").contains(section) && !line.isBlank())
                metadata.computeIfAbsent(section,k->new StringBuilder()).append(line).append('\n');
            for(String sentence:line.split("[!?;]+|\\.(?=\\s|$)")) {
                var mentioned=catalog.stream().filter(skill->vocabulary.mentioned(skill,sentence)).toList();
                boolean required=REQUIRED.matcher(sentence).find(),preferred=PREFERRED.matcher(sentence).find();
                boolean ambiguous=NEGATION.matcher(sentence).find() || (required&&preferred)
                        || ((required||preferred)&&mentioned.size()>1);
                String kind=ambiguous?"UNCLASSIFIED":preferred?"PREFERRED":required?"REQUIRED"
                        :Set.of("REQUIRED","PREFERRED").contains(section)?section:"UNCLASSIFIED";
                for(var skill:mentioned)requirements.merge(skill.getName(),kind,(a,b)->rank(a)<=rank(b)?a:b);
            }
        }
        return new Draft(VERSION,description,field(metadata,"title",300),field(metadata,"responsibilities",10000),
                field(metadata,"experience",3000),field(metadata,"location",1000),field(metadata,"technologies",3000),
                names(requirements,"REQUIRED"),names(requirements,"PREFERRED"),names(requirements,"UNCLASSIFIED"),
                "English catalog-name suggestions only. Extraction may miss skills or misread context. Review against the full description, add missing requirements and correct each category. Experience, location and responsibilities are not scored; no employer proficiency is inferred.");
    }
    private static int rank(String value) { return value.equals("REQUIRED")?0:value.equals("PREFERRED")?1:2; }
    private static List<String> names(Map<String,String> values,String kind) { return values.entrySet().stream().filter(e->e.getValue().equals(kind)).map(Map.Entry::getKey).toList(); }
    private static String field(Map<String,StringBuilder> fields,String name,int max) {
        String value=fields.getOrDefault(name,new StringBuilder()).toString().strip();
        return value.substring(0,Math.min(value.length(),max));
    }
}
