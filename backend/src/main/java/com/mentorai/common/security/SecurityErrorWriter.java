package com.mentorai.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentorai.common.config.RequestIdFilter;
import com.mentorai.common.exception.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class SecurityErrorWriter {

    private final ObjectMapper objectMapper;

    public SecurityErrorWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(
            HttpServletRequest request,
            HttpServletResponse response,
            int status,
            String code,
            String message) throws IOException {
        Object requestId = request.getAttribute(RequestIdFilter.ATTRIBUTE);
        ApiError error = new ApiError(
                Instant.now(), status, code, message, request.getRequestURI(),
                requestId == null ? "unavailable" : requestId.toString(), Map.of());
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
