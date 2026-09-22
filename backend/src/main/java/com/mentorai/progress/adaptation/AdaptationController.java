package com.mentorai.progress.adaptation;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class AdaptationController {
    private final AdaptationService service;
    public AdaptationController(AdaptationService service) { this.service=service; }
    @GetMapping("/api/adaptations/{id}")
    public AdaptationResponse get(Authentication auth, @PathVariable UUID id) { return service.get(auth,id); }
    @GetMapping("/api/roadmaps/{id}/adaptations")
    public AdaptationResponse.History history(Authentication auth, @PathVariable UUID id, @RequestParam(defaultValue="0") int page) {
        return service.history(auth,id,page);
    }
    @PostMapping("/api/adaptations/{id}/accept")
    public AdaptationResponse accept(Authentication auth, @PathVariable UUID id, @Valid @RequestBody AcceptAdaptationRequest request) {
        return service.accept(auth,id,request);
    }
}
