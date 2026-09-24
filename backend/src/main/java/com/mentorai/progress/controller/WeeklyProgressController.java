package com.mentorai.progress.controller;

import com.mentorai.progress.dto.CheckInHistoryResponse;
import com.mentorai.progress.dto.CheckInRequest;
import com.mentorai.progress.dto.CheckInResponse;
import com.mentorai.progress.dto.CreateWeeklyPlanRequest;
import com.mentorai.progress.dto.WeeklyPlanResponse;
import com.mentorai.progress.service.WeeklyProgressService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WeeklyProgressController {
    private final WeeklyProgressService progress;

    public WeeklyProgressController(WeeklyProgressService progress) {
        this.progress = progress;
    }

    @PostMapping("/api/weekly-plan")
    public ResponseEntity<WeeklyPlanResponse> create(
            Authentication authentication,
            @Valid @RequestBody CreateWeeklyPlanRequest request) {
        WeeklyPlanResponse response = progress.create(authentication, request);
        URI location = URI.create("/api/weekly-plan/current?weekStart=" + response.weekStart());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/api/weekly-plan/current")
    public WeeklyPlanResponse currentPlan(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return progress.currentPlan(authentication, weekStart);
    }

    @GetMapping("/api/check-ins/current")
    public CheckInResponse currentCheckIn(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return progress.currentCheckIn(authentication, weekStart);
    }

    @GetMapping("/api/check-ins/history")
    public CheckInHistoryResponse history(Authentication authentication, @RequestParam(defaultValue = "0") int page) {
        return progress.history(authentication, page);
    }

    @PostMapping("/api/check-ins")
    public ResponseEntity<CheckInResponse> submit(
            Authentication authentication,
            @Valid @RequestBody CheckInRequest request) {
        CheckInResponse response = progress.submit(authentication, request);
        URI location = URI.create("/api/check-ins/current?weekStart=" + response.weekStart());
        return ResponseEntity.created(location).body(response);
    }
}
