package com.mentorai.jobs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import com.mentorai.skills.entity.Skill;
import com.mentorai.skills.repository.SkillRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

class JobExtractionTest {
    private JobExtractionService extractor() {
        var repository=mock(SkillRepository.class);
        when(repository.findControlledVocabulary()).thenReturn(List.of(new Skill("Java","java","Language"),
                new Skill("React","react","Frontend"),new Skill("SQL","sql","Database"),new Skill("Docker","docker","Tools")));
        return new JobExtractionService(new JobSkillVocabulary(repository));
    }
    @Test void localCueCannotPromoteOtherSkillsInMixedContext() {
        var draft=extractor().extract("Java is required, while our frontend uses React. Java is required, SQL is a bonus.");
        assertThat(draft.requiredSkills()).isEmpty();
        assertThat(draft.unclassifiedSkills()).containsExactly("Java","React","SQL");
    }
    @Test void unsupportedInlineHeadingStopsRequiredSection() {
        var draft=extractor().extract("Required skills:\nJava\nBenefits: Learn Docker with us");
        assertThat(draft.requiredSkills()).containsExactly("Java");
        assertThat(draft.unclassifiedSkills()).containsExactly("Docker");
    }
    @Test void straightAndCurlyContractedNegationsRemainUnclassified() {
        var draft=extractor().extract("Java isn't required. SQL isn’t mandatory. React doesn't have to be required. Docker needn’t be preferred.");
        assertThat(draft.requiredSkills()).isEmpty();assertThat(draft.preferredSkills()).isEmpty();
        assertThat(draft.unclassifiedSkills()).containsExactly("Docker","Java","React","SQL");
    }
    @Test void explicitSectionsStillClassifyListsAndCatalogBoundaryIsRespected() {
        var draft=extractor().extract("Required skills:\nJava and SQL\nPreferred skills:\nReact\nTechnologies: JavaScript and Docker");
        assertThat(draft.requiredSkills()).containsExactly("Java","SQL");
        assertThat(draft.preferredSkills()).containsExactly("React");
        assertThat(draft.unclassifiedSkills()).containsExactly("Docker");
    }
}
