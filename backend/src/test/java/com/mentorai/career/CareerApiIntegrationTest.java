package com.mentorai.career;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.Set;
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
class CareerApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void careerEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/careers")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/careers/by-slug/backend-developer"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/careers/analyze")).andExpect(status().isUnauthorized());
    }

    @Test
    void analysisIsRepeatableAndReturnsUniqueIdsForRenderedLists() throws Exception {
        String token = register("Repeat Student", "career-repeat@example.com");
        mockMvc.perform(put("/api/profile").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"interests":["Backend"],"timeAvailablePerWeek":8}
                                """))
                .andExpect(status().isOk());
        JsonNode first = analyze(token);
        JsonNode second = analyze(token);
        assertThat(first.get("candidates")).isEqualTo(second.get("candidates"));
        Set<String> candidateIds = new HashSet<>();
        for (JsonNode candidate : first.get("candidates")) {
            assertThat(candidate.path("careerId").asText()).isNotBlank();
            assertThat(candidateIds.add(candidate.get("careerId").asText())).isTrue();
            Set<String> gapIds = new HashSet<>();
            for (JsonNode gap : candidate.get("skillGaps")) {
                assertThat(gap.path("skillId").asText()).isNotBlank();
                assertThat(gapIds.add(gap.get("skillId").asText())).isTrue();
            }
            Set<String> alternativeIds = new HashSet<>();
            for (JsonNode alternative : candidate.get("alternatives")) {
                assertThat(alternative.path("id").asText()).isNotBlank();
                assertThat(alternativeIds.add(alternative.get("id").asText())).isTrue();
                assertThat(alternative.get("id")).isNotEqualTo(candidate.get("careerId"));
            }
        }
    }

    @Test
    void analysisRejectsInvalidLimitsAndUnknownCareers() throws Exception {
        String token = register("Boundary Student", "career-boundary@example.com");
        mockMvc.perform(post("/api/careers/analyze?limit=11")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/careers/by-slug/not-a-career")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    private JsonNode analyze(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/careers/analyze")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    @Test
    void exposesTheControlledCareerCatalogAndRealityDetail() throws Exception {
        String token = register("Catalog Student", "career-catalog@example.com");

        mockMvc.perform(get("/api/careers").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[*].slug", hasItem("backend-developer")))
                .andExpect(jsonPath("$[0].dataLabel", is("CONTROLLED CATALOG DATA")));

        mockMvc.perform(get("/api/careers/by-slug/ai-ml-engineer")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("AI/ML Engineer")))
                .andExpect(jsonPath("$.entryDifficulty", is("VERY_HIGH")))
                .andExpect(jsonPath("$.marketEvidenceStatus", is("INSUFFICIENT_MARKET_EVIDENCE")))
                .andExpect(jsonPath("$.realitySummary", containsString("Beginners should establish")))
                .andExpect(jsonPath("$.skills", hasSize(7)));
    }

    @Test
    void requiresAtLeastOneProfileSignalBeforeAnalysis() throws Exception {
        String token = register("Empty Student", "career-empty@example.com");

        mockMvc.perform(post("/api/careers/analyze").header("Authorization", bearer(token)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code", is("PROFILE_INCOMPLETE")));
    }

    @Test
    void ranksWithTransparentProfileOnlyFactorsAndReportsSkillGaps() throws Exception {
        String token = register("Backend Student", "career-backend@example.com");
        String profile = """
                {
                  "degree": "BCA",
                  "year": 2,
                  "interests": ["Backend development", "APIs"],
                  "goals": ["Become a backend developer"],
                  "preferredDomains": ["Backend"],
                  "programmingLanguages": ["Java"],
                  "timeAvailablePerWeek": 12,
                  "skills": [
                    {"name":"Java","category":"Programming language","proficiency":"INTERMEDIATE","confidence":"HIGH","source":"SELF_REPORTED"},
                    {"name":"SQL","category":"Database","proficiency":"BEGINNER","confidence":"MEDIUM","source":"SELF_REPORTED"},
                    {"name":"Git","category":"Developer tool","proficiency":"BEGINNER","confidence":"MEDIUM","source":"SELF_REPORTED"}
                  ]
                }
                """;

        mockMvc.perform(put("/api/profile")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profile))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/careers/analyze?limit=5")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calculationVersion", is("career-fit-v1")))
                .andExpect(jsonPath("$.scoreName", is("Career Fit Indicator")))
                .andExpect(jsonPath("$.scoreMode", is("PROFILE_ONLY_NO_MARKET_EVIDENCE")))
                .andExpect(jsonPath("$.availableEvidenceWeight", is(85)))
                .andExpect(jsonPath("$.configuredWeights.marketCompatibility", is(15)))
                .andExpect(jsonPath("$.candidates", hasSize(5)))
                .andExpect(jsonPath("$.candidates[0].slug", is("backend-developer")))
                .andExpect(jsonPath("$.candidates[0].careerFitIndicator", greaterThan(50)))
                .andExpect(jsonPath("$.candidates[0].factors.interestAlignment", is(100)))
                .andExpect(jsonPath("$.candidates[0].factors.marketCompatibility").doesNotExist())
                .andExpect(jsonPath("$.candidates[0].skillGaps[*].name", hasItem("Spring Boot")))
                .andExpect(jsonPath("$.candidates[0].uncertainties[*]",
                        hasItem("No validated market observations are available in Phase 2.")));
    }

    private String register(String name, String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"displayName":"%s","email":"%s","password":"correct-horse-battery-staple"}
                                """.formatted(name, email)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("accessToken").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
