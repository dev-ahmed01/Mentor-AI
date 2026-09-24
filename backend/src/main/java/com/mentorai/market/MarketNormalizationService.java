package com.mentorai.market;

import com.mentorai.market.MarketModels.*;
import com.mentorai.skills.entity.Skill;
import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class MarketNormalizationService {
    public static final String VERSION="market-normalization-v1";
    public Optional<Observation> normalize(RawJob job,Instant now,List<Skill> skills) {
        if(job==null || !bounded(job.sourceId(),300) || !bounded(job.sourceUrl(),1500)
                || !bounded(job.title(),300) || !bounded(job.company(),300) || !bounded(job.location(),300)
                || !bounded(job.description(),50000) || !bounded(job.rawPayload(),100000)
                || job.publishedAt()==null || job.publishedAt().isAfter(now)
                || job.publishedAt().isBefore(now.minusSeconds(30L*86400)))return Optional.empty();
        String url;
        try {
            URI uri=URI.create(job.sourceUrl());
            if(!"https".equalsIgnoreCase(uri.getScheme()) || !"www.arbeitnow.com".equalsIgnoreCase(uri.getHost())
                    || uri.getUserInfo()!=null || uri.getPort()!=-1 || !uri.getPath().startsWith("/jobs/"))return Optional.empty();
            url=new URI("https",null,"www.arbeitnow.com",-1,uri.getPath().replaceAll("/+$",""),null,null).toString();
        } catch(Exception ignored) { return Optional.empty(); }
        String text=plain(job.description());
        if(plain(job.title()).isBlank() || plain(job.company()).isBlank() || plain(job.location()).isBlank())return Optional.empty();
        List<Mention> mentions=new ArrayList<>();
        Map<UUID,Pattern> matchers=new HashMap<>();
        for(Skill skill:skills) {
            List<String> aliases=new ArrayList<>(List.of(skill.getName()));
            if(skill.getName().equals("REST APIs"))aliases.addAll(List.of("REST API","RESTful"));
            if(skill.getName().equals("Node.js"))aliases.add("NodeJS");
            if(skill.getName().equals("PostgreSQL"))aliases.add("Postgres");
            matchers.put(skill.getId(),Pattern.compile("(?iu)(?<![\\p{L}\\p{N}])(?:"+
                    String.join("|",aliases.stream().map(Pattern::quote).toList())+")(?![\\p{L}\\p{N}])"));
        }
        String[] sentences=text.split("[!?;\\n]+|\\.(?=\\s|$)");
        Map<String,Long> matchedCounts=new HashMap<>();
        for(String sentence:sentences)matchedCounts.computeIfAbsent(sentence,s->matchers.values().stream().filter(p->p.matcher(s).find()).count());
        for(Skill skill:skills) {
            String requirement=null;
            for(String sentence:sentences) {
                if(!matchers.get(skill.getId()).matcher(sentence).find())continue;
                // Multiple named skills make cue attribution ambiguous; keep their mentions unclassified.
                String found=matchedCounts.get(sentence)>1 || Pattern.compile("(?i)\\b(not|no|never)\\b").matcher(sentence).find()?"UNSPECIFIED"
                        :Pattern.compile("(?i)\\b(preferred|optional|nice to have|desirable)\\b").matcher(sentence).find()?"PREFERRED"
                        :Pattern.compile("(?i)\\b(required|mandatory|must have)\\b").matcher(sentence).find()?"REQUIRED":"UNSPECIFIED";
                if(requirement==null || found.equals("REQUIRED") || requirement.equals("UNSPECIFIED"))requirement=found;
            }
            if(requirement!=null)mentions.add(new Mention(skill.getId(),skill.getName(),requirement));
        }
        mentions.sort(Comparator.comparing(Mention::name).thenComparing(Mention::skillId));
        return Optional.of(new Observation(UUID.randomUUID(),ArbeitnowProvider.SOURCE,job.sourceId().strip(),url,now,job.publishedAt(),
                plain(job.title()).strip(),plain(job.company()).strip(),plain(job.location()).strip(),job.remote(),VERSION,List.copyOf(mentions)));
    }
    public static boolean contains(String text,String phrase) {
        return Pattern.compile("(?iu)(?<![\\p{L}\\p{N}])"+Pattern.quote(phrase)+"(?![\\p{L}\\p{N}])").matcher(text).find();
    }
    public static String key(String text) { return text.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+"," ").strip(); }
    private boolean bounded(String text,int max) { return text!=null && !text.isBlank() && text.length()<=max; }
    private static String plain(String text) {
        return text.replaceAll("(?is)<(script|style)[^>]*>.*?</\\1>"," ")
                .replaceAll("(?i)</(?:p|li|h[1-6])>|<br\\s*/?>","\n").replaceAll("<[^>]*>"," ")
                .replace("&nbsp;"," ").replace("&amp;","&").replace("&lt;","<").replace("&gt;",">").replace("&quot;","\"");
    }
}
