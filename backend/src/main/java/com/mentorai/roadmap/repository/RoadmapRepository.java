package com.mentorai.roadmap.repository;

import com.mentorai.roadmap.entity.Roadmap;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapRepository extends JpaRepository<Roadmap, UUID> {
    Optional<Roadmap> findByIdAndUserId(UUID id, UUID userId);
    Optional<Roadmap> findFirstByUserIdOrderByCreatedAtDescIdDesc(UUID userId);
}
