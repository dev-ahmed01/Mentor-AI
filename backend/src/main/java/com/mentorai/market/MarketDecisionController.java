package com.mentorai.market;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/market")
public class MarketDecisionController {
    private final MarketDecisionService decisions;
    public MarketDecisionController(MarketDecisionService decisions) { this.decisions=decisions; }
    @PostMapping("/snapshots/{id}/decisions") @ResponseStatus(HttpStatus.CREATED)
    public MarketDecisionService.Decision create(@PathVariable UUID id,Authentication auth) { return decisions.create(id,auth); }
    @GetMapping("/decisions/{id}")
    public MarketDecisionService.Decision get(@PathVariable UUID id,Authentication auth) { return decisions.get(id,auth); }
}
