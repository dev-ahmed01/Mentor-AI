package com.mentorai.auth;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthAndProfileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerAuthenticateAndUpdateNormalizedProfile() throws Exception {
        String token = register("Student One", "student1@example.com");

        mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("student1@example.com")))
                .andExpect(jsonPath("$.displayName", is("Student One")));

        String profile = """
                {
                  "degree": " BCA ",
                  "year": 2,
                  "semester": 3,
                  "interests": ["Backend development", "backend development"],
                  "goals": ["Get a software internship"],
                  "programmingLanguages": ["Java"],
                  "preferredDomains": ["Backend"],
                  "targetLocations": ["India", "Remote"],
                  "remotePreference": "FLEXIBLE",
                  "timeAvailablePerWeek": 12,
                  "skills": [
                    {
                      "name": "Java",
                      "category": "Programming language",
                      "proficiency": "INTERMEDIATE",
                      "confidence": "MEDIUM",
                      "source": "SELF_REPORTED"
                    },
                    {
                      "name": " java ",
                      "category": "Programming language",
                      "proficiency": "BEGINNER",
                      "confidence": "LOW",
                      "source": "SELF_REPORTED"
                    }
                  ],
                  "currentProjects": ["Student REST API"],
                  "experience": "Course projects",
                  "certifications": [],
                  "shortTermGoal": "Build a Spring Boot API",
                  "longTermGoal": "Become job-ready for backend roles",
                  "avoidances": ["Too many frameworks at once"]
                }
                """;

        mockMvc.perform(put("/api/profile")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.degree", is("BCA")))
                .andExpect(jsonPath("$.interests", hasSize(1)))
                .andExpect(jsonPath("$.skills", hasSize(1)))
                .andExpect(jsonPath("$.skills[0].name", is("Java")))
                .andExpect(jsonPath("$.updatedAt", not(blankOrNullString())));
    }

    @Test
    void profilesAreIsolatedByAuthenticatedUser() throws Exception {
        String first = register("First", "first@example.com");
        String second = register("Second", "second@example.com");

        mockMvc.perform(put("/api/profile")
                        .header("Authorization", bearer(first))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"degree\":\"BCA\",\"year\":2}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/profile").header("Authorization", bearer(second)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.degree").doesNotExist());
    }

    @Test
    void rejectsDuplicateAccountAndInvalidProfile() throws Exception {
        String token = register("Student", "duplicate@example.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationBody("Student", "duplicate@example.com")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("RESOURCE_CONFLICT")));

        mockMvc.perform(put("/api/profile")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"year\":99}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")))
                .andExpect(jsonPath("$.fieldErrors.year").exists());
    }

    @Test
    void unauthenticatedRequestsUseSafeConsistentErrorContract() throws Exception {
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("AUTHENTICATION_REQUIRED")))
                .andExpect(jsonPath("$.message", is("Authentication is required to access this resource.")))
                .andExpect(jsonPath("$.requestId", not(blankOrNullString())))
                .andExpect(jsonPath("$.requestId", not(is("unavailable"))));
    }

    private String register(String name, String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationBody(name, email)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("accessToken").asText();
    }

    private String registrationBody(String name, String email) {
        return """
                {"displayName":"%s","email":"%s","password":"correct-horse-battery-staple"}
                """.formatted(name, email);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
