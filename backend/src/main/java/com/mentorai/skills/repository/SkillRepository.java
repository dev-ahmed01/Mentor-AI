package com.mentorai.skills.repository;

import com.mentorai.skills.entity.Skill;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, UUID> {

    Optional<Skill> findByNormalizedName(String normalizedName);

    @Query(value="select * from skills where id in (select skill_id from career_skills union select skill_id from skill_dependencies union select prerequisite_skill_id from skill_dependencies)",nativeQuery=true)
    List<Skill> findControlledVocabulary();
}
