package com.mentorai.progress.adaptation;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdaptationRepository extends JpaRepository<Adaptation, UUID> {
    Optional<Adaptation> findByIdAndUserId(UUID id, UUID userId);
    Optional<Adaptation> findByCheckInIdAndUserId(UUID checkInId, UUID userId);
    Page<Adaptation> findByRoadmapIdAndUserIdOrderByCreatedAtDescIdDesc(UUID roadmapId, UUID userId, Pageable pageable);
}
