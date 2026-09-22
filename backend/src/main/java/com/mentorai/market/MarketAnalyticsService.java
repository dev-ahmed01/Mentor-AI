package com.mentorai.market;

import com.mentorai.career.entity.Career;
import com.mentorai.market.MarketModels.*;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class MarketAnalyticsService {
    public static final String VERSION="market-snapshot-v1";
    public Snapshot aggregate(Career career,List<Observation> observations,Instant now) {
        var titles=new TreeSet<>(career.getCommonJobTitles());
        titles.add(career.getName());
        var relevant=observations.stream().filter(o->titles.stream().anyMatch(t->MarketNormalizationService.contains(o.title(),t)))
                .sorted(Comparator.comparing(Observation::sourceUrl)).toList();
        Map<UUID,int[]> counts=new HashMap<>();
        Map<UUID,String> names=new HashMap<>();
        for(var observation:relevant)for(var skill:observation.skills()) {
            names.put(skill.skillId(),skill.name());
            int[] totals=counts.computeIfAbsent(skill.skillId(),ignored->new int[4]);
            totals[0]++;
            totals[switch(skill.requirement()){case "REQUIRED"->1;case "PREFERRED"->2;default->3;}]++;
        }
        var frequencies=counts.entrySet().stream().map(e->new Frequency(e.getKey(),names.get(e.getKey()),e.getValue()[0],e.getValue()[1],e.getValue()[2],e.getValue()[3]))
                .sorted(Comparator.comparingInt(Frequency::mentions).reversed().thenComparing(Frequency::name)).toList();
        return new Snapshot(UUID.randomUUID(),career.getId(),career.getName(),ArbeitnowProvider.SOURCE,"https://www.arbeitnow.com",
                "At most the first 100 records from one page of Arbeitnow's Germany-focused public feed. Locations remain as supplied; remote does not mean worldwide eligibility.",
                now,now.minusSeconds(30L*86400),now,now.plusSeconds(72L*3600),relevant.size(),
                (int)relevant.stream().map(o->MarketNormalizationService.key(o.company())).distinct().count(),
                (int)relevant.stream().filter(o->!o.skills().isEmpty()).count(),10,VERSION,List.copyOf(titles),frequencies,relevant,
                List.of("Bounded single-source sample, not total vacancies, a market trend or hiring probability.",
                        "Title matching is conservative and may miss synonyms or non-English titles. Seniority and work eligibility are not filtered.",
                        "Skill mentions use a limited catalog and local keyword rules, not verified employer requirements.",
                        "Unspecified mentions are not required skills. Listings may close or change after collection.",
                        "Eligibility thresholds are product guardrails, not statistical confidence estimates."));
    }
    public Evidence evidence(Snapshot snapshot,Instant now) {
        String status=!now.isBefore(snapshot.freshUntil())?"STALE"
                :snapshot.sampleSize()<snapshot.minimumSampleSize()?"INSUFFICIENT_SAMPLE"
                :snapshot.employerCount()<3?"INSUFFICIENT_EMPLOYERS"
                :snapshot.listingsWithSkills()*100<snapshot.sampleSize()*60?"LOW_SKILL_COVERAGE":"AVAILABLE";
        String message=switch(status) {
            case "STALE"->"Evidence is more than 72 hours old. It is excluded from new market decisions.";
            case "INSUFFICIENT_SAMPLE"->"Fewer than 10 distinct listings match this career. Market scoring is disabled.";
            case "INSUFFICIENT_EMPLOYERS"->"Fewer than 3 distinct employers are represented. Market scoring is disabled.";
            case "LOW_SKILL_COVERAGE"->"Catalog skills were found in fewer than 60% of listings. Market scoring is disabled.";
            default->"This sample meets the documented guardrails. Evaluate it only if this source and location context suit your goals.";
        };
        return new Evidence(status,snapshot,message);
    }
}
