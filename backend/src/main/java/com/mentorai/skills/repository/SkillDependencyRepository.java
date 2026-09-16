package com.mentorai.skills.repository;

import com.mentorai.skills.entity.SkillDependency;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SkillDependencyRepository extends JpaRepository<SkillDependency, UUID> {
    @Query("select d from SkillDependency d join fetch d.skill join fetch d.prerequisite "
            + "order by d.skill.name, d.prerequisite.name")
    List<SkillDependency> findGraph();
}
