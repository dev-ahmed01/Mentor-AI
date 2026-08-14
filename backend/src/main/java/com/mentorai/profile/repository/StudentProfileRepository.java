package com.mentorai.profile.repository;

import com.mentorai.profile.entity.StudentProfile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, UUID> {

    @EntityGraph(attributePaths = {"skills", "skills.skill"})
    Optional<StudentProfile> findByUserId(UUID userId);
}
