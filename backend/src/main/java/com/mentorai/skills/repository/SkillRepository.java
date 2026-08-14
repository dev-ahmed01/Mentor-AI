package com.mentorai.skills.repository;

import com.mentorai.skills.entity.Skill;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, UUID> {

    Optional<Skill> findByNormalizedName(String normalizedName);
}
