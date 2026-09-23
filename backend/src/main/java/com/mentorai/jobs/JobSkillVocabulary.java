package com.mentorai.jobs;

import com.mentorai.skills.entity.Skill;
import com.mentorai.skills.repository.SkillRepository;
import java.util.*;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/** Only curated career/dependency skills may influence job comparisons. */
@Service
public class JobSkillVocabulary {
    private final SkillRepository skills;
    public JobSkillVocabulary(SkillRepository skills) { this.skills=skills; }
    public List<Skill> all() { return skills.findControlledVocabulary().stream().sorted(Comparator.comparing(Skill::getName)).toList(); }
    public List<String> aliases(Skill skill) {
        return switch(skill.getName()) {
            case "REST APIs" -> List.of("REST APIs","REST API","RESTful");
            case "Node.js" -> List.of("Node.js","NodeJS");
            case "PostgreSQL" -> List.of("PostgreSQL","Postgres");
            default -> List.of(skill.getName());
        };
    }
    public boolean mentioned(Skill skill,String text) {
        return aliases(skill).stream().anyMatch(alias->Pattern.compile("(?iu)(?<![\\p{L}\\p{N}])"+Pattern.quote(alias)+"(?![\\p{L}\\p{N}])").matcher(text).find());
    }
    public Optional<Skill> resolve(String name,List<Skill> catalog) {
        String key=key(name);
        return catalog.stream().filter(s->aliases(s).stream().anyMatch(a->key(a).equals(key))).findFirst();
    }
    public static String key(String name) { return name.strip().toLowerCase(Locale.ROOT).replaceAll("\\s+"," "); }
}
