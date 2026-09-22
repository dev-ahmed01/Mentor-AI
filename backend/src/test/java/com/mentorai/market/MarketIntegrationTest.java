package com.mentorai.market;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import com.mentorai.market.MarketModels.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class MarketIntegrationTest {
    static final UUID CAREER=UUID.fromString("20000000-0000-0000-0000-000000000001");
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired MarketIngestionService ingestion;
    @Autowired MarketAnalyticsService analytics;
    @Autowired MarketRepository repository;
    @MockitoBean MarketDataProvider provider;

    @BeforeEach void cleanEvidence() {
        jdbc.update("delete from market_decisions");
        jdbc.update("delete from market_snapshot_observations");
        jdbc.update("delete from market_snapshots");
        jdbc.update("delete from market_observations");
        jdbc.update("update market_sources set last_attempt_at=null,last_success_at=null,last_status='NEVER_COLLECTED'");
    }

    @Test void noEvidenceIsExplicitAndRequiresAuthentication() throws Exception {
        mvc.perform(get("/api/market").param("careerId",CAREER.toString())).andExpect(status().isUnauthorized());
        JsonNode response=call("GET","/api/market?careerId="+CAREER,register(),null,200);
        assertThat(response.path("status").asText()).isEqualTo("UNAVAILABLE");
        assertThat(response.path("snapshot").isMissingNode()).isTrue();
    }

    @Test void migrationHasNoInventedEvidenceAndSourceStartsDisabled() {
        assertThat(jdbc.queryForObject("select count(*) from market_observations",Integer.class)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from market_snapshots",Integer.class)).isZero();
    }

    @Test void aggregatesDistinctPostingsAndPreservesProvenanceAcrossReingestion() throws Exception {
        Instant now=Instant.now().truncatedTo(ChronoUnit.SECONDS);
        List<RawJob> batch=new ArrayList<>(jobs(10,now));
        batch.add(batch.getFirst());
        batch.add(new RawJob("repost", "https://www.arbeitnow.com/jobs/repost",batch.getFirst().title(),batch.getFirst().company(),"Berlin",false,now.minusSeconds(60),"Java required.","{}"));
        var result=ingestion.ingest(batch,now);
        assertThat(result.accepted()).isEqualTo(10);
        assertThat(result.duplicates()).isEqualTo(2);
        Snapshot snapshot=repository.latest(CAREER).orElseThrow();
        assertThat(analytics.evidence(snapshot,now).status()).isEqualTo("AVAILABLE");
        assertThat(snapshot.sampleSize()).isEqualTo(10);
        assertThat(snapshot.employerCount()).isEqualTo(10);
        Frequency java=snapshot.skills().stream().filter(s->s.name().equals("Java")).findFirst().orElseThrow();
        assertThat(java.mentions()).isEqualTo(10);
        assertThat(java.required()).isEqualTo(10);
        assertThat(snapshot.skills().stream().filter(s->s.name().equals("SQL")).findFirst().orElseThrow().preferred()).isEqualTo(5);
        String token=register();
        var observation=call("GET","/api/market/observations/"+snapshot.observations().getFirst().id(),token,null,200);
        assertThat(observation.path("source").asText()).isEqualTo("ARBEITNOW");
        assertThat(observation.path("sourceUrl").asText()).startsWith("https://www.arbeitnow.com/jobs/");
        assertThat(observation.has("rawPayload")).isFalse();
        String frozen=repository.encode(snapshot);
        ingestion.ingest(jobs(10,now),now.plusSeconds(3600));
        assertThat(jdbc.queryForObject("select count(*) from market_observations",Integer.class)).isEqualTo(10);
        assertThat(repository.encode(repository.snapshot(snapshot.id()))).isEqualTo(frozen);
        assertThat(repository.latest(CAREER).orElseThrow().sampleSize()).isEqualTo(10);
    }

    @Test void rejectsUnsafeFutureOldAndMalformedObservationsAndDoesNotInventSkillRequirements() {
        Instant now=Instant.now();
        RawJob base=jobs(1,now).getFirst();
        var invalid=List.of(new RawJob("bad","javascript:alert(1)",base.title(),"Evil","Berlin",false,now,"Java","{}"),
                new RawJob("future",base.sourceUrl(),base.title(),"Future","Berlin",false,now.plusSeconds(3600),"Java","{}"),
                new RawJob("old",base.sourceUrl(),base.title(),"Old","Berlin",false,now.minus(31,ChronoUnit.DAYS),"Java","{}"),
                new RawJob("missing",base.sourceUrl(),"","Missing","Berlin",false,now,"Java","{}"));
        assertThat(ingestion.ingest(invalid,now).rejected()).isEqualTo(4);
        var mention=new RawJob("safe","https://www.arbeitnow.com/jobs/safe?utm_source=x",base.title(),"Safe","Remote - Germany",true,now.minusSeconds(1),
                "<script>Java required</script><p>JavaScript and React are our tools. SQL optional.</p>","{}");
        ingestion.ingest(List.of(mention),now.plusSeconds(1));
        var snapshot=repository.latest(CAREER).orElseThrow();
        assertThat(snapshot.skills().stream().map(Frequency::name)).doesNotContain("Java");
        assertThat(snapshot.skills().stream().filter(s->s.name().equals("React")).findFirst().orElseThrow().unspecified()).isEqualTo(1);
        assertThat(snapshot.observations().getFirst().sourceUrl()).doesNotContain("?");
        assertThat(snapshot.observations().getFirst().location()).isEqualTo("Remote - Germany");
    }

    @Test void staleSmallLowCoverageAndSingleEmployerSamplesAreIneligible() {
        Instant now=Instant.now();
        ingestion.ingest(jobs(9,now),now);
        assertThat(analytics.evidence(repository.latest(CAREER).orElseThrow(),now).status()).isEqualTo("INSUFFICIENT_SAMPLE");
        ingestion.ingest(jobs(10,now),now.plusSeconds(1));
        var enough=repository.latest(CAREER).orElseThrow();
        assertThat(analytics.evidence(enough,now.plus(4,ChronoUnit.DAYS)).status()).isEqualTo("STALE");
        var unknown=jobs(10,now).stream().map(j->new RawJob(j.sourceId()+"x",j.sourceUrl()+"x",j.title(),j.company(),j.location(),j.remote(),j.publishedAt(),"No catalog technologies listed.","{}")).toList();
        ingestion.ingest(unknown,now.plusSeconds(2));
        assertThat(analytics.evidence(repository.latest(CAREER).orElseThrow(),now).status()).isEqualTo("LOW_SKILL_COVERAGE");
        var single=jobs(10,now).stream().map(j->new RawJob(j.sourceId()+"s",j.sourceUrl()+"s",j.title()+" "+j.sourceId(),"One employer",j.location(),j.remote(),j.publishedAt(),j.description(),"{}")).toList();
        ingestion.ingest(single,now.plusSeconds(3));
        assertThat(analytics.evidence(repository.latest(CAREER).orElseThrow(),now).status()).isEqualTo("INSUFFICIENT_EMPLOYERS");
    }

    @Test void failureKeepsPriorEvidenceAndCooldownPreventsRepeatedProviderCalls() throws Exception {
        Instant now=Instant.now();
        ingestion.ingest(jobs(10,now),now);
        UUID prior=repository.latest(CAREER).orElseThrow().id();
        when(provider.fetch()).thenThrow(new IllegalStateException("upstream unavailable"));
        assertThat(ingestion.refresh().status()).isEqualTo("FAILED");
        assertThat(ingestion.refresh().status()).isEqualTo("COOLDOWN");
        verify(provider,times(1)).fetch();
        assertThat(repository.latest(CAREER).orElseThrow().id()).isEqualTo(prior);
        assertThat(repository.source().lastStatus()).isEqualTo("FAILED");
    }

    @Test void rejectedProviderBatchDoesNotMarkCollectionSuccessful() throws Exception {
        when(provider.fetch()).thenReturn(List.of(new RawJob("bad","https://evil.test/jobs/a","Bad","Bad","Berlin",false,Instant.now(),"Java","{}")));
        assertThat(ingestion.refresh().status()).isEqualTo("REJECTED");
        assertThat(repository.source().lastStatus()).isEqualTo("FAILED");
        assertThat(repository.source().lastSuccessAt()).isNull();
    }

    @Test void studentCreatedSkillsCannotChangeSharedEvidenceEligibility() throws Exception {
        String token=register();
        call("PUT","/api/profile",token,Map.of("skills",List.of(Map.of("name","SentinelCustomKeyword","category","Custom","proficiency","ADVANCED","confidence","HIGH","source","SELF_REPORTED"))),200);
        Instant now=Instant.now();
        var batch=jobs(10,now).stream().map(j->new RawJob(j.sourceId(),j.sourceUrl(),j.title(),j.company(),j.location(),j.remote(),j.publishedAt(),"SentinelCustomKeyword required.",j.rawPayload())).toList();
        ingestion.ingest(batch,now);
        var snapshot=repository.latest(CAREER).orElseThrow();
        assertThat(snapshot.skills()).isEmpty();
        assertThat(analytics.evidence(snapshot,now).status()).isEqualTo("LOW_SKILL_COVERAGE");
    }

    @Test void savedDecisionsActivateOnlyQualifiedEvidenceAndAreOwnerIsolatedAndImmutable() throws Exception {
        Instant now=Instant.now();
        ingestion.ingest(jobs(10,now),now);
        UUID snapshot=repository.latest(CAREER).orElseThrow().id();
        String token=register();
        call("PUT","/api/profile",token,Map.of("timeAvailablePerWeek",8,"interests",List.of("backend"),"goals",List.of("build APIs"),
                "programmingLanguages",List.of("Java"),"preferredDomains",List.of("web"),"shortTermGoal","Learn Spring","longTermGoal","Backend engineer",
                "skills",List.of(Map.of("name","Java","category","Language","proficiency","INTERMEDIATE","confidence","MEDIUM","source","SELF_REPORTED"))),200);
        JsonNode before=call("GET","/api/profile",token,null,200);
        JsonNode decision=call("POST","/api/market/snapshots/"+snapshot+"/decisions",token,null,201);
        assertThat(decision.path("marketWeight").asInt()).isEqualTo(15);
        assertThat(decision.path("marketCompatibility").asInt()).isEqualTo(67);
        var factors=decision.path("profileOnlyCareer").path("factors");
        int expected=(int)Math.round((20*factors.path("interestAlignment").asInt()+15*factors.path("goalAlignment").asInt()
                +30*factors.path("skillAlignment").asInt()+15*factors.path("entryAccessibility").asInt()
                +5*factors.path("learningEffortCompatibility").asInt()+15*67)/100.0);
        assertThat(decision.path("careerFitIndicator").asInt()).isEqualTo(expected);
        assertThat(decision.path("priorities").path("marketWeight").asInt()).isEqualTo(10);
        assertThat(decision.path("profileInputs").path("interests").get(0).asText()).isEqualTo("backend");
        assertThat(decision.path("profileInputs").path("skills").get(0).path("confidence").asText()).isEqualTo("MEDIUM");
        assertThat(decision.path("profileInputs").path("shortTermGoal").asText()).isEqualTo("Learn Spring");
        assertThat(decision.path("profileScoringVersion").asText()).isEqualTo("career-fit-v1");
        assertThat(call("GET","/api/profile",token,null,200)).isEqualTo(before);
        call("PUT","/api/profile",token,Map.of("timeAvailablePerWeek",1,"skills",List.of()),200);
        assertThat(call("GET","/api/market/decisions/"+decision.path("id").asText(),token,null,200)).isEqualTo(decision);
        call("GET","/api/market/decisions/"+decision.path("id").asText(),register(),null,404);
        JsonNode base=call("GET","/api/decisions/learning-priorities?careerId="+CAREER,token,null,200);
        assertThat(base.path("marketWeight").asInt()).isZero();
        assertThat(base.path("calculationVersion").asText()).isEqualTo("learning-priorities-v1");
    }

    @Test void insufficientOrStaleSnapshotsKeepMarketWeightsZeroAndSkillGatesRemain() throws Exception {
        Instant now=Instant.now();
        String token=register();
        ingestion.ingest(jobs(3,now),now);
        var small=repository.latest(CAREER).orElseThrow();
        var result=call("POST","/api/market/snapshots/"+small.id()+"/decisions",token,null,201);
        assertThat(result.path("marketWeight").asInt()).isZero();
        assertThat(result.path("careerFitIndicator")).isEqualTo(result.path("profileOnlyCareer").path("careerFitIndicator"));
        assertThat(result.path("priorities").path("marketWeight").asInt()).isZero();
        Instant old=now.minus(4,ChronoUnit.DAYS);
        ingestion.ingest(jobs(10,old),old);
        var snapshots=jdbc.queryForList("select id from market_snapshots where career_id=? and collected_at<?",UUID.class,CAREER,java.sql.Timestamp.from(now.minusSeconds(60)));
        var stale=call("POST","/api/market/snapshots/"+snapshots.getFirst()+"/decisions",token,null,201);
        assertThat(stale.path("evidence").path("status").asText()).isEqualTo("STALE");
        assertThat(stale.path("marketWeight").asInt()).isZero();
        ingestion.ingest(jobs(10,now),now.plusSeconds(1));
        var fresh=call("POST","/api/market/snapshots/"+repository.latest(CAREER).orElseThrow().id()+"/decisions",token,null,201);
        for(var item:fresh.path("priorities").path("decisions")) {
            if(!item.path("prerequisiteReadiness").path("eligible").asBoolean())assertThat(item.path("priority").asText()).isEqualTo("NOT_YET");
        }
        assertThat(fresh.path("priorities").path("immediateFocusLimit").asInt()).isZero();
    }

    static List<RawJob> jobs(int count,Instant now) {
        List<RawJob> result=new ArrayList<>();
        for(int i=0;i<count;i++)result.add(new RawJob("fixture-"+i,"https://www.arbeitnow.com/jobs/fixture-"+i,
                "Senior Backend Developer", "DEMO Employer "+i,"Berlin",false,now.minusSeconds(60),
                "<p>Java required.</p>"+(i<5?"<p>SQL preferred.</p>":""),"{\"fixture\":"+i+"}"));
        return result;
    }

    String register() throws Exception {
        return call("POST","/api/auth/register",null,Map.of("displayName","DEMO Market Verification","email","market-"+UUID.randomUUID()+"@example.com","password","market-verification-only-0922"),201).path("accessToken").asText();
    }
    JsonNode call(String method,String path,String token,Object body,int expected) throws Exception {
        var request=switch(method){case "POST"->post(path);case "PUT"->put(path);default->get(path);};
        if(token!=null)request.header("Authorization","Bearer "+token);
        if(body!=null)request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(body));
        return json.readTree(mvc.perform(request).andExpect(status().is(expected)).andReturn().getResponse().getContentAsByteArray());
    }
}
