package com.mentorai.career.repository;

import com.mentorai.career.entity.Career;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareerRepository extends JpaRepository<Career, UUID> {

    @EntityGraph(attributePaths = {"skills", "skills.skill"})
    List<Career> findAllByActiveTrueOrderByNameAsc();

    @EntityGraph(attributePaths = {"skills", "skills.skill"})
    Optional<Career> findByIdAndActiveTrue(UUID id);

    @EntityGraph(attributePaths = {"skills", "skills.skill"})
    Optional<Career> findBySlugAndActiveTrue(String slug);
}
