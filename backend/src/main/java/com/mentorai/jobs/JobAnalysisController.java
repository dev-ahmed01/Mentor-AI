package com.mentorai.jobs;

import com.mentorai.jobs.JobModels.*;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/jobs")
public class JobAnalysisController {
    private final JobExtractionService extraction;
    private final JobAnalysisService analyses;
    public JobAnalysisController(JobExtractionService extraction,JobAnalysisService analyses) { this.extraction=extraction;this.analyses=analyses; }
    @PostMapping("/extract")
    public Draft extract(@Valid @RequestBody ExtractRequest request) { return extraction.extract(request.description()); }
    @PostMapping("/analyses") @ResponseStatus(HttpStatus.CREATED)
    public Analysis create(@Valid @RequestBody AnalysisRequest request,Authentication authentication) { return analyses.create(request,authentication); }
    @GetMapping("/analyses/{id}")
    public Analysis get(@PathVariable UUID id,Authentication authentication) { return analyses.get(id,authentication); }
}
