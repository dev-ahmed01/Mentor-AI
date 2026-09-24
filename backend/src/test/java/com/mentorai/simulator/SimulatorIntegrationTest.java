package com.mentorai.simulator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class SimulatorIntegrationTest {
    private static final String CAREER="20000000-0000-0000-0000-000000000001";
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;

    @Test void learningJavaSatisfiesOneCareerRequirementAndUnlocksOnlyTheNextFoundation() throws Exception {
        String token=register(); profile(token, List.of());
        JsonNode real=call("GET","/api/decisions/learning-priorities?careerId="+CAREER,token,null,200);
        JsonNode result=simulate(token,"Java");
        assertThat(result.path("before").path("priorities")).isEqualTo(real);
        assertThat(result.path("before").path("requiredSatisfied").asInt()).isZero();
        assertThat(result.path("after").path("requiredSatisfied").asInt()).isEqualTo(1);
        assertThat(result.path("newlySatisfiedRequirements").findValuesAsText("name")).containsExactly("Java");
        assertThat(result.path("newlyEligibleSkills").findValuesAsText("name")).contains("Spring Fundamentals").doesNotContain("Spring Boot");
        assertThat(result.path("resultLabel").asText()).isEqualTo("SIMULATION");
        assertThat(result.path("marketEvidenceStatus").asText()).isEqualTo("UNAVAILABLE");
        assertThat(result.path("after").path("priorities").path("marketWeight").asInt()).isZero();
    }

    @Test void missingAncestorsAreNotInventedWhenSimulatingAnAdvancedSkill() throws Exception {
        String token=register(); profile(token,List.of());
        JsonNode result=simulate(token,"Spring Fundamentals");
        assertThat(result.path("selectedSkillPrerequisites").path("eligible").asBoolean()).isFalse();
        assertThat(result.path("newlyEligibleSkills").findValuesAsText("name")).doesNotContain("Spring Boot");
        JsonNode boot=decision(result,"after","Spring Boot");
        assertThat(boot.path("prerequisiteReadiness").path("eligible").asBoolean()).isFalse();
        assertThat(boot.path("prerequisiteReadiness").path("prerequisites").findValuesAsText("name")).contains("Java");
    }

    @Test void foundationOnlySkillUnlocksDependentWhenAllOtherAncestorsAreRecorded() throws Exception {
        String token=register(); profile(token,List.of(skill("Java","BEGINNER")));
        JsonNode result=simulate(token,"Spring Fundamentals");
        assertThat(result.path("newlyEligibleSkills").findValuesAsText("name")).contains("Spring Boot");
        assertThat(result.path("newlySatisfiedRequirements")).isEmpty();
        assertThat(decision(result,"after","Spring Boot").path("priority").asText()).isNotEqualTo("NOT_YET");
    }

    @Test void higherProficiencyIsPreservedAndRepeatedSimulationDoesNotAccumulate() throws Exception {
        String token=register(); profile(token,List.of(skill("Java","ADVANCED")));
        JsonNode result=simulate(token,"Java");
        assertThat(result.path("assumedProficiency").asText()).isEqualTo("ADVANCED");
        assertThat(result.path("noChange").asBoolean()).isTrue();
        assertThat(result.path("before")).isEqualTo(result.path("after"));
        assertThat(simulate(token,"Java")).isEqualTo(result);
    }

    @Test void unrelatedSkillAndEmptyProfileReturnUsefulNoEffectOrNoTimeStates() throws Exception {
        String token=register();
        JsonNode unrelated=simulate(token,"HTML");
        assertThat(unrelated.path("noChange").asBoolean()).isTrue();
        assertThat(unrelated.path("before")).isEqualTo(unrelated.path("after"));
        JsonNode result=simulate(token,"Java");
        assertThat(result.path("after").path("priorities").path("immediateFocusLimit").asInt()).isZero();
        assertThat(result.path("newlySatisfiedRequirements").findValuesAsText("name")).contains("Java");
    }

    @Test void simulationLeavesProfileRoadmapPlansCheckInsAndAdaptationsUnchanged() throws Exception {
        String token=register(); profile(token,List.of());
        JsonNode roadmap=call("POST","/api/roadmaps",token,Map.of("careerId",CAREER),201);
        JsonNode plan=call("POST","/api/weekly-plan",token,Map.of("roadmapId",roadmap.path("id").asText()),201);
        List<Object> outcomes=new ArrayList<>();
        plan.path("tasks").forEach(t -> outcomes.add(Map.of("taskId",t.path("taskId").asText(),"outcome","MISSED")));
        call("POST","/api/check-ins",token,Map.of("planId",plan.path("id").asText(),"expectedRoadmapRevision",plan.path("roadmapRevision").asLong(),
                "actualHours",0,"availableHoursNextWeek",8,"energyOrCapacityBand","LOW","blockers",List.of(),"tasks",outcomes),201);
        List<String> paths=List.of("/api/profile","/api/roadmaps/"+roadmap.path("id").asText(),"/api/check-ins/history",
                "/api/roadmaps/"+roadmap.path("id").asText()+"/adaptations","/api/decisions/learning-priorities?careerId="+CAREER);
        Map<String,JsonNode> before=new LinkedHashMap<>();
        for (var path:paths) before.put(path,call("GET",path,token,null,200));
        var counts=counts();
        simulate(token,"Java"); simulate(token,"Spring Fundamentals");
        for (var path:paths) assertThat(call("GET",path,token,null,200)).isEqualTo(before.get(path));
        assertThat(counts()).isEqualTo(counts);
        String other=register();
        assertThat(simulate(other,"Java").path("before").path("requiredSatisfied").asInt()).isZero();
    }

    @Test void rejectsUnauthenticatedMalformedAndUnknownInputs() throws Exception {
        mvc.perform(post("/api/simulator/skill").contentType(MediaType.APPLICATION_JSON).content("{}")) .andExpect(status().isUnauthorized());
        String token=register();
        call("POST","/api/simulator/skill",token,Map.of(),400);
        call("POST","/api/simulator/skill",token,Map.of("skillId","not-a-uuid","targetCareerId",CAREER),400);
        call("POST","/api/simulator/skill",token,Map.of("skillId",UUID.randomUUID(),"targetCareerId",CAREER),404);
        call("POST","/api/simulator/skill",token,Map.of("skillId",id("Java"),"targetCareerId",UUID.randomUUID()),404);
    }

    private Map<String,Integer> counts() {
        Map<String,Integer> result=new LinkedHashMap<>();
        for (var table:List.of("student_profiles","student_skills","roadmaps","roadmap_tasks","weekly_plans","weekly_check_ins","roadmap_adaptations"))
            result.put(table,jdbc.queryForObject("SELECT COUNT(*) FROM "+table,Integer.class));
        return result;
    }
    private JsonNode decision(JsonNode result,String state,String name) {
        for (var item:result.path(state).path("priorities").path("decisions")) if (item.path("name").asText().equals(name)) return item;
        throw new AssertionError("Missing decision "+name);
    }
    private UUID id(String name) { return jdbc.queryForObject("SELECT id FROM skills WHERE name=?",UUID.class,name); }
    private JsonNode simulate(String token,String name) throws Exception {
        return call("POST","/api/simulator/skill",token,Map.of("skillId",id(name),"targetCareerId",CAREER),200);
    }
    private Map<String,Object> skill(String name,String proficiency) { return Map.of("name",name,"category","Language","proficiency",proficiency,"confidence","MEDIUM","source","SELF_REPORTED"); }
    private void profile(String token,List<?> skills) throws Exception { call("PUT","/api/profile",token,Map.of("timeAvailablePerWeek",8,"skills",skills),200); }
    private String register() throws Exception {
        return "Bearer "+call("POST","/api/auth/register","",Map.of("displayName","Simulator student","email",UUID.randomUUID()+"@example.com","password","simulator-demo-test-password"),201).path("accessToken").asText();
    }
    private JsonNode call(String method,String path,String token,Object body,int expected) throws Exception {
        var request=org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request(org.springframework.http.HttpMethod.valueOf(method),path).header("Authorization",token).contentType(MediaType.APPLICATION_JSON);
        if(body!=null)request.content(json.writeValueAsString(body));
        String response=mvc.perform(request).andExpect(status().is(expected)).andReturn().getResponse().getContentAsString();
        return response.isBlank()?json.createObjectNode():json.readTree(response);
    }
}
