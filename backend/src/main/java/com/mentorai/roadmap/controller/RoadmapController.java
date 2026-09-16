package com.mentorai.roadmap.controller;

import com.mentorai.roadmap.dto.*;
import com.mentorai.roadmap.service.RoadmapService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roadmaps")
public class RoadmapController {
    private final RoadmapService roadmaps;
    public RoadmapController(RoadmapService roadmaps) { this.roadmaps = roadmaps; }

    @PostMapping
    public ResponseEntity<RoadmapResponse> create(Authentication authentication, @Valid @RequestBody CreateRoadmapRequest request) {
        var result = roadmaps.create(authentication, request);
        return ResponseEntity.created(URI.create("/api/roadmaps/" + result.id())).body(result);
    }
    @GetMapping("/current")
    public RoadmapResponse current(Authentication authentication) { return roadmaps.current(authentication); }
    @GetMapping("/{id}")
    public RoadmapResponse get(Authentication authentication, @PathVariable UUID id) { return roadmaps.get(authentication, id); }
    @PutMapping("/{id}")
    public RoadmapResponse update(Authentication authentication, @PathVariable UUID id, @Valid @RequestBody UpdateRoadmapRequest request) {
        return roadmaps.update(authentication, id, request);
    }
}
