package com.mentorai.career.dto;

import java.util.UUID;

public record AlternativeCareerResponse(UUID id, String slug, String name, int careerFitIndicator) {
}
