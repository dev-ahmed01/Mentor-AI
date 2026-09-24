package com.mentorai.pivot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class PivotApiIntegrationTest {
    static final String BACKEND="20000000-0000-0000-0000-000000000001", DATA="20000000-0000-0000-0000-000000000006";
    @Autowired MockMvc mvc; @Autowired ObjectMapper json;

    @Test void previewPreservesStateAndAcceptanceCarriesCompletedSkillsWithoutChangingProfileOrHistory() throws Exception {
        String token=register(); var source=create(token,BACKEND);
        source=edit(token,source,"SQL","COMPLETED");
        var profile=call("GET","/api/profile",token,null,200);
        var weekly=call("POST","/api/weekly-plan",token,Map.of("roadmapId",source.path("id").asText()),201);
        var preview=preview(token,source,DATA); var comparison=preview.path("comparison");
        assertThat(comparison.path("skippableSkills").toString()).contains("SQL");
        assertThat(comparison.path("newlyRequiredSkills").toString()).contains("Python");
        assertThat(comparison.path("changedPriorities")).isNotEmpty();
        assertThat(comparison.path("credits").toString()).contains("COMPLETED_TASK");
        assertThat(call("GET","/api/roadmaps/current",token,null,200)).isEqualTo(source);
        var accepted=accept(token,preview,200);
        assertThat(accepted.path("status").asText()).isEqualTo("ACCEPTED");
        assertThat(accept(token,preview,200)).isEqualTo(accepted);
        var revised=call("GET","/api/roadmaps/current",token,null,200);
        assertThat(revised.path("id")).isEqualTo(accepted.path("acceptedRoadmapId"));
        assertThat(revised.path("previousRoadmapId")).isEqualTo(source.path("id"));
        assertThat(task(revised,"SQL").path("satisfiedAtGeneration").asBoolean()).isTrue();
        assertThat(call("GET","/api/roadmaps/"+source.path("id").asText(),token,null,200)).isEqualTo(source);
        assertThat(call("GET","/api/profile",token,null,200)).isEqualTo(profile);
        assertThat(call("GET","/api/weekly-plan/current",token,null,200)).isEqualTo(weekly);
        var next=preview(token,revised,BACKEND);
        assertThat(next.path("comparison").path("skippableSkills").toString()).contains("SQL");
    }

    @Test void skippedUnknownAndReopenedTasksDoNotSupplyCreditAndSnapshotStaysFrozen() throws Exception {
        String token=register(); var source=create(token,BACKEND);
        source=edit(token,source,"SQL","SKIPPED");
        var preview=preview(token,source,DATA);
        assertThat(preview.path("comparison").path("skippableSkills").toString()).doesNotContain("SQL");
        source=edit(token,source,"SQL","NOT_STARTED"); source=edit(token,source,"SQL","COMPLETED");
        source=edit(token,source,"SQL","NEEDS_REVIEW");
        assertThat(preview(token,source,DATA).path("comparison").path("credits")).isEmpty();
        assertThat(call("GET","/api/pivots/"+preview.path("id").asText(),token,null,200)).isEqualTo(preview);
        accept(token,preview,409);
    }

    @Test void ownershipInputsAndCurrentSourceAreEnforced() throws Exception {
        call("GET","/api/pivots",null,null,401);
        String token=register(), other=register();var source=create(token,BACKEND);
        call("POST","/api/pivots",token,Map.of("sourceRoadmapId",source.path("id").asText(),"targetCareerId",BACKEND),400);
        call("POST","/api/pivots",token,Map.of("sourceRoadmapId",source.path("id").asText(),"targetCareerId",UUID.randomUUID()),404);
        call("POST","/api/pivots",other,Map.of("sourceRoadmapId",source.path("id").asText(),"targetCareerId",DATA),404);
        var preview=preview(token,source,DATA);String path="/api/pivots/"+preview.path("id").asText();
        call("GET",path,other,null,404);call("POST",path+"/accept",other,Map.of("expectedSourceRevision",0),404);
        assertThat(call("GET","/api/pivots",other,null,200)).isEmpty();
        call("POST",path+"/accept",token,Map.of(),400);
        create(token,BACKEND);accept(token,preview,409);
        call("POST","/api/pivots",token,Map.of("sourceRoadmapId",source.path("id").asText(),"targetCareerId",DATA),409);
    }

    @Test void profileChangeInvalidatesPreviewWithoutCreatingRoadmap() throws Exception {
        String token=register();var source=create(token,BACKEND);var preview=preview(token,source,DATA);
        call("PUT","/api/profile",token,Map.of("timeAvailablePerWeek",4),200);
        accept(token,preview,409);
        assertThat(call("GET","/api/roadmaps/current",token,null,200)).isEqualTo(source);
    }

    @Test void simultaneousIdenticalAcceptanceReturnsOneRoadmapAndCompetingPreviewIsRejected() throws Exception {
        String token=register();var source=create(token,BACKEND);var preview=preview(token,source,DATA);var competitor=preview(token,source,DATA);
        try(var pool=Executors.newFixedThreadPool(2)) {
            var start=new CountDownLatch(1);
            Callable<JsonNode> action=()->{start.await();return accept(token,preview,200);};
            var first=pool.submit(action);var second=pool.submit(action);start.countDown();
            assertThat(first.get(20,TimeUnit.SECONDS)).isEqualTo(second.get(20,TimeUnit.SECONDS));
        }
        accept(token,competitor,409);
    }

    @Test void partialKnowledgeTransfersWithoutSkippingAndAllTargetsMetHasZeroEffort() throws Exception {
        String token=register();var source=create(token,BACKEND);
        call("PUT","/api/profile",token,Map.of("timeAvailablePerWeek",8,"skills",List.of(skill("SQL","BEGINNER"),skill("Python","BEGINNER"))),200);
        var preview=preview(token,source,DATA);var comparison=preview.path("comparison");
        assertThat(comparison.path("transferableSkills").toString()).contains("SQL","Python");
        assertThat(comparison.path("skippableSkills").toString()).doesNotContain("SQL","Python");
        assertThat(comparison.path("satisfiedPrerequisites")).isEmpty(); // No edges modeled for Data Engineer in the starter graph.
        assertThat(preview(token,source,"20000000-0000-0000-0000-000000000007").path("comparison").path("satisfiedPrerequisites").toString()).contains("Python");
        int hours=0;for(var stage:comparison.path("proposedStages"))for(var task:stage.path("tasks"))hours+=task.path("estimatedHours").asInt();
        assertThat(comparison.path("effort").path("illustrativeHours").asInt()).isEqualTo(hours);
        assertThat(comparison.path("effort").path("capacityWeeks").asInt()).isEqualTo((hours+7)/8);
        var advanced=new ArrayList<Map<String,String>>();for(var d:comparison.path("after").path("decisions"))advanced.add(skill(d.path("name").asText(),"ADVANCED"));
        call("PUT","/api/profile",token,Map.of("timeAvailablePerWeek",8,"skills",advanced),200);
        var allMet=preview(token,source,DATA);
        assertThat(allMet.path("comparison").path("effort").path("band").asText()).isEqualTo("TARGETS_MET");
        assertThat(allMet.path("comparison").path("effort").path("illustrativeHours").asInt()).isZero();
        accept(token,allMet,200);
        assertThat(call("GET","/api/roadmaps/current",token,null,200).hasNonNull("nextAction")).isFalse();
    }

    @Test void missingWeeklyAvailabilityBlocksPreview() throws Exception {
        String token=register();var source=create(token,BACKEND);
        call("PUT","/api/profile",token,Map.of(),200);
        call("POST","/api/pivots",token,Map.of("sourceRoadmapId",source.path("id").asText(),"targetCareerId",DATA),422);
    }

    @Test void concurrentDistinctPreviewsOnlyOneCanBeAccepted() throws Exception {
        String token=register();var source=create(token,BACKEND);var a=preview(token,source,DATA);var b=preview(token,source,DATA);
        try(var pool=Executors.newFixedThreadPool(2)) {
            var start=new CountDownLatch(1);
            Callable<Integer> first=()->{start.await();return acceptStatus(token,a);};
            Callable<Integer> second=()->{start.await();return acceptStatus(token,b);};
            var f=pool.submit(first);var s=pool.submit(second);start.countDown();
            assertThat(List.of(f.get(20,TimeUnit.SECONDS),s.get(20,TimeUnit.SECONDS))).containsExactlyInAnyOrder(200,409);
        }
    }
    @Test void strongerCompletedCreditSurvivesLowerTargetInIntermediatePivot() throws Exception {
        String token=register();var frontend=create(token,"20000000-0000-0000-0000-000000000003");
        frontend=edit(token,frontend,"HTML","COMPLETED");
        var toFullStack=preview(token,frontend,"20000000-0000-0000-0000-000000000002");accept(token,toFullStack,200);
        var fullStack=call("GET","/api/roadmaps/current",token,null,200);
        assertThat(task(fullStack,"HTML").path("targetProficiency").asText()).isEqualTo("BEGINNER");
        var back=preview(token,fullStack,"20000000-0000-0000-0000-000000000003");
        assertThat(back.path("comparison").path("skippableSkills").toString()).contains("HTML");
        var html=java.util.stream.StreamSupport.stream(back.path("comparison").path("credits").spliterator(),false).filter(c->c.path("name").asText().equals("HTML")).findFirst().orElseThrow();
        assertThat(html.path("proficiency").asText()).isEqualTo("INTERMEDIATE");
    }
    @Test void manuallyReskippingReopenedCreditDoesNotRestoreIt() throws Exception {
        String token=register();var source=edit(token,create(token,BACKEND),"SQL","COMPLETED");
        accept(token,preview(token,source,DATA),200);var revised=call("GET","/api/roadmaps/current",token,null,200);
        revised=edit(token,revised,"SQL","NEEDS_REVIEW");revised=edit(token,revised,"SQL","SKIPPED");
        assertThat(preview(token,revised,BACKEND).path("comparison").path("skippableSkills").toString()).doesNotContain("SQL");
        revised=edit(token,revised,"SQL","NOT_STARTED");revised=edit(token,revised,"SQL","COMPLETED");
        assertThat(preview(token,revised,BACKEND).path("comparison").path("skippableSkills").toString()).contains("SQL");
    }
    @Test void acceptanceSerializesWithGenerationProfileAndSourceChanges() throws Exception {
        for(String operation:List.of("generation","profile","source")) {
            String token=register();var source=create(token,BACKEND);var preview=preview(token,source,DATA);
            try(var pool=Executors.newFixedThreadPool(2)) {
                var start=new CountDownLatch(1);
                var acceptance=pool.submit(()->{start.await();return acceptStatus(token,preview);});
                var change=pool.submit(()->{start.await();return switch(operation){
                    case "generation"->create(token,BACKEND);
                    case "profile"->call("PUT","/api/profile",token,Map.of("timeAvailablePerWeek",4),200);
                    default->edit(token,source,"SQL","COMPLETED");
                };});
                start.countDown();int accepted=acceptance.get(20,TimeUnit.SECONDS);var changed=change.get(20,TimeUnit.SECONDS);
                assertThat(accepted).isIn(200,409);
                var saved=call("GET","/api/pivots/"+preview.path("id").asText(),token,null,200);
                assertThat(saved.path("comparison")).isEqualTo(preview.path("comparison"));
                var current=call("GET","/api/roadmaps/current",token,null,200);
                if(operation.equals("generation")) {
                    assertThat(current.path("id")).isEqualTo(changed.path("id"));
                    assertThat(current.path("previousRoadmapId")).isEqualTo(accepted==200?saved.path("acceptedRoadmapId"):source.path("id"));
                } else {
                    assertThat(current.path("id")).isEqualTo(accepted==200?saved.path("acceptedRoadmapId"):source.path("id"));
                }
            }
        }
    }
    int acceptStatus(String token,JsonNode preview) throws Exception {
        return mvc.perform(post("/api/pivots/"+preview.path("id").asText()+"/accept").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsBytes(Map.of("expectedSourceRevision",preview.path("comparison").path("sourceRoadmap").path("revision").asLong())))).andReturn().getResponse().getStatus();
    }
    Map<String,String> skill(String name,String proficiency){return Map.of("name",name,"category","DEMO","proficiency",proficiency,"confidence","MEDIUM","source","SELF_REPORTED");}
    JsonNode accept(String token,JsonNode preview,int status) throws Exception {return call("POST","/api/pivots/"+preview.path("id").asText()+"/accept",token,Map.of("expectedSourceRevision",preview.path("comparison").path("sourceRoadmap").path("revision").asLong()),status);}
    JsonNode preview(String token,JsonNode source,String target) throws Exception {return call("POST","/api/pivots",token,Map.of("sourceRoadmapId",source.path("id").asText(),"targetCareerId",target),201);}
    JsonNode create(String token,String career) throws Exception {return call("POST","/api/roadmaps",token,Map.of("careerId",career),201);}
    JsonNode task(JsonNode roadmap,String name) {for(var phase:roadmap.path("phases"))for(var task:phase.path("tasks"))if(task.path("skillName").asText().equals(name))return task;throw new AssertionError("Missing task "+name);}
    JsonNode edit(String token,JsonNode source,String name,String state) throws Exception {var task=task(source,name);return call("PUT","/api/roadmaps/"+source.path("id").asText(),token,Map.of("expectedRevision",source.path("revision").asLong(),"tasks",List.of(Map.of("id",task.path("id").asText(),"title",task.path("title").asText(),"estimatedHours",8,"state",state))),200);}
    String register() throws Exception {String token=call("POST","/api/auth/register",null,Map.of("displayName","DEMO Pivot Test","email","pivot-"+UUID.randomUUID()+"@example.com","password","pivot-test-password-0923"),201).path("accessToken").asText();call("PUT","/api/profile",token,Map.of("timeAvailablePerWeek",8),200);return token;}
    JsonNode call(String method,String path,String token,Object body,int expected) throws Exception {
        var request=switch(method){case "POST"->post(path);case "PUT"->put(path);default->get(path);};
        if(token!=null)request.header("Authorization","Bearer "+token);
        if(body!=null)request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(body));
        return json.readTree(mvc.perform(request).andExpect(status().is(expected)).andReturn().getResponse().getContentAsByteArray());
    }
}
