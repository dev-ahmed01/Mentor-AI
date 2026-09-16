package com.mentorai.career.service;

import com.mentorai.career.dto.CareerDetailResponse;
import com.mentorai.career.dto.CareerSummaryResponse;
import com.mentorai.career.entity.Career;
import com.mentorai.career.repository.CareerRepository;
import com.mentorai.common.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CareerCatalogService {

    private final CareerRepository careerRepository;

    public CareerCatalogService(CareerRepository careerRepository) {
        this.careerRepository = careerRepository;
    }

    @Transactional(readOnly = true)
    public List<CareerSummaryResponse> list() {
        return careerRepository.findAllByActiveTrueOrderByNameAsc().stream()
                .map(CareerSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CareerDetailResponse get(UUID id) {
        Career career = careerRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Career was not found."));
        return CareerDetailResponse.from(career);
    }

    @Transactional(readOnly = true)
    public CareerDetailResponse getBySlug(String slug) {
        Career career = careerRepository.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Career was not found."));
        return CareerDetailResponse.from(career);
    }
}
