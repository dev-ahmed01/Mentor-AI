package com.mentorai.career.controller;

import com.mentorai.career.dto.CareerAnalysisResponse;
import com.mentorai.career.dto.CareerDetailResponse;
import com.mentorai.career.dto.CareerSummaryResponse;
import com.mentorai.career.service.CareerAnalysisService;
import com.mentorai.career.service.CareerCatalogService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/careers")
@Validated
public class CareerController {

    private final CareerCatalogService catalogService;
    private final CareerAnalysisService analysisService;

    public CareerController(CareerCatalogService catalogService, CareerAnalysisService analysisService) {
        this.catalogService = catalogService;
        this.analysisService = analysisService;
    }

    @GetMapping
    List<CareerSummaryResponse> list() {
        return catalogService.list();
    }

    @GetMapping("/{id}")
    CareerDetailResponse get(@PathVariable UUID id) {
        return catalogService.get(id);
    }

    @GetMapping("/by-slug/{slug}")
    CareerDetailResponse getBySlug(@PathVariable String slug) {
        return catalogService.getBySlug(slug);
    }

    @PostMapping("/analyze")
    CareerAnalysisResponse analyze(
            Authentication authentication,
            @RequestParam(defaultValue = "5") @Min(3) @Max(10) int limit) {
        return analysisService.analyze(authentication, limit);
    }
}
