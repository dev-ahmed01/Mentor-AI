package com.mentorai.market;

import com.mentorai.career.repository.CareerRepository;
import com.mentorai.common.exception.ResourceNotFoundException;
import com.mentorai.market.MarketModels.*;
import java.time.Instant;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/market")
public class MarketController {
    private final MarketRepository repository;
    private final MarketAnalyticsService analytics;
    private final CareerRepository careers;
    public MarketController(MarketRepository repository,MarketAnalyticsService analytics,CareerRepository careers) {
        this.repository=repository;this.analytics=analytics;this.careers=careers;
    }
    @GetMapping public Evidence latest(@RequestParam UUID careerId) {
        if(!careers.existsById(careerId))throw new ResourceNotFoundException("Career was not found.");
        return repository.latest(careerId).map(s->analytics.evidence(s,Instant.now()))
                .orElse(new Evidence("UNAVAILABLE",null,"No collected evidence is available. Profile-only decisions remain available."));
    }
    @GetMapping("/sources") public SourceState source() { return repository.source(); }
    @GetMapping("/snapshots/{id}") public Evidence snapshot(@PathVariable UUID id) { return analytics.evidence(repository.snapshot(id),Instant.now()); }
    @GetMapping("/observations/{id}") public Observation observation(@PathVariable UUID id) { return repository.observation(id); }
}
