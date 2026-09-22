package com.mentorai.market;

import static org.assertj.core.api.Assertions.assertThat;
import com.mentorai.market.MarketModels.*;
import com.mentorai.skills.entity.Skill;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class MarketNormalizationTest {
    private final MarketNormalizationService service=new MarketNormalizationService();
    private final List<Skill> catalog=List.of(new Skill("Java","java","Language"),new Skill("JavaScript","javascript","Language"),
            new Skill("Node.js","node.js","Framework"),new Skill("SQL","sql","Database"));

    @Test void dottedNamesAndNegatedRequirementsAreNotMisclassified() {
        var observation=normalize("Node.js required. Java is not required. SQL is optional. JavaScript tools.");
        assertThat(observation.skills()).anySatisfy(s->{assertThat(s.name()).isEqualTo("Node.js");assertThat(s.requirement()).isEqualTo("REQUIRED");});
        assertThat(observation.skills()).anySatisfy(s->{assertThat(s.name()).isEqualTo("Java");assertThat(s.requirement()).isEqualTo("UNSPECIFIED");});
        assertThat(observation.skills()).anySatisfy(s->{assertThat(s.name()).isEqualTo("SQL");assertThat(s.requirement()).isEqualTo("PREFERRED");});
    }

    @Test void rejectsEmptyVisibleIdentityAndDoesNotFollowUntrustedUrls() {
        Instant now=Instant.now();
        for(String url:List.of("https://www.arbeitnow.com.evil.test/jobs/a","http://www.arbeitnow.com/jobs/a","https://user@www.arbeitnow.com/jobs/a","https://127.0.0.1/jobs/a"))
            assertThat(service.normalize(new RawJob("a",url,"Backend Developer","A","Berlin",false,now,"Java","{}"),now,catalog)).isEmpty();
        assertThat(service.normalize(new RawJob("a","https://www.arbeitnow.com/jobs/a","<script>fake</script>","A","Berlin",false,now,"Java","{}"),now,catalog)).isEmpty();
    }

    @Test void requirementCuesDoNotLeakAcrossSkillsInTheSameSentence() {
        var mixed=normalize("Java is required, while SQL is optional.");
        assertThat(mixed.skills()).allSatisfy(s->assertThat(s.requirement()).isEqualTo("UNSPECIFIED"));
        var ambiguous=normalize("Java is used by our team, and SQL is required.");
        assertThat(ambiguous.skills()).allSatisfy(s->assertThat(s.requirement()).isEqualTo("UNSPECIFIED"));
    }
    private Observation normalize(String text) {
        Instant now=Instant.now();
        return service.normalize(new RawJob("a","https://www.arbeitnow.com/jobs/a","Backend Developer","A","Berlin",false,now,text,"{}"),now,catalog).orElseThrow();
    }
}
