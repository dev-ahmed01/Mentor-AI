package com.mentorai.market;

import com.mentorai.career.repository.CareerRepository;
import com.mentorai.skills.repository.SkillRepository;
import com.mentorai.market.MarketModels.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class MarketIngestionService {
    private final MarketDataProvider provider;
    private final MarketRepository repository;
    private final MarketNormalizationService normalization;
    private final MarketAnalyticsService analytics;
    private final CareerRepository careers;
    private final SkillRepository skills;
    private final TransactionTemplate transactions;
    public MarketIngestionService(MarketDataProvider provider,MarketRepository repository,MarketNormalizationService normalization,
                                  MarketAnalyticsService analytics,CareerRepository careers,SkillRepository skills,TransactionTemplate transactions) {
        this.provider=provider;this.repository=repository;this.normalization=normalization;this.analytics=analytics;
        this.careers=careers;this.skills=skills;this.transactions=transactions;
    }
    public IngestionResult refresh() {
        Instant now=Instant.now();
        if(!Boolean.TRUE.equals(transactions.execute(tx->repository.claim(now))))return new IngestionResult("COOLDOWN",0,0,0,0,0);
        try {
            var result=ingest(provider.fetch(),now);
            if(result.status().equals("SUCCESS"))repository.succeeded(now); else repository.failed();
            return result;
        } catch(Exception error) {
            if(error instanceof InterruptedException)Thread.currentThread().interrupt();
            org.slf4j.LoggerFactory.getLogger(getClass()).warn("Market collection failed ({})",error.getClass().getSimpleName());
            repository.failed();
            return new IngestionResult("FAILED",0,0,0,0,0);
        }
    }
    /** Internal pipeline boundary, also used by deterministic fixture tests. Never exposed as an import API. */
    public IngestionResult ingest(List<RawJob> jobs,Instant now) {
        if(jobs==null || jobs.size()>100)throw new IllegalArgumentException("Provider page exceeds the supported bound.");
        return transactions.execute(tx->{
            // Student profiles may create global skill rows; only curated career/graph IDs are evidence vocabulary.
            var catalog=skills.findAllById(repository.controlledSkillIds());
            List<Observation> accepted=new ArrayList<>();
            Set<String> urls=new HashSet<>(),identities=new HashSet<>();
            int rejected=0,duplicates=0;
            for(var job:jobs) {
                var normalized=normalization.normalize(job,now,catalog);
                if(normalized.isEmpty()) { rejected++; continue; }
                var item=normalized.get();
                String identity=MarketNormalizationService.key(item.company())+"|"+MarketNormalizationService.key(item.title())+"|"+MarketNormalizationService.key(item.location());
                if(urls.contains(item.sourceUrl()) || identities.contains(identity)) { duplicates++; continue; }
                urls.add(item.sourceUrl());identities.add(identity);
                String hash=hash(repository.encode(job)+MarketNormalizationService.VERSION);
                accepted.add(repository.store(item,hash,job.rawPayload()));
            }
            // An all-invalid nonempty response is an upstream failure, not a fresh empty market.
            if(!jobs.isEmpty() && accepted.isEmpty())return new IngestionResult("REJECTED",jobs.size(),0,rejected,duplicates,0);
            var active=careers.findAllByActiveTrueOrderByNameAsc();
            for(var career:active)repository.snapshot(analytics.aggregate(career,accepted,now));
            return new IngestionResult("SUCCESS",jobs.size(),accepted.size(),rejected,duplicates,active.size());
        });
    }
    private String hash(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch(Exception e) { throw new IllegalStateException(e); }
    }
}
