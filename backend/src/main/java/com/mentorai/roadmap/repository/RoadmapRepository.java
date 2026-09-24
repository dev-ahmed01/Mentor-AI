package com.mentorai.roadmap.repository;

import com.mentorai.roadmap.entity.Roadmap;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface RoadmapRepository extends JpaRepository<Roadmap, UUID> {
    Optional<Roadmap> findByIdAndUserId(UUID id, UUID userId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Roadmap r where r.id = :id and r.userId = :userId")
    Optional<Roadmap> findOwnedForUpdate(@Param("id") UUID id, @Param("userId") UUID userId);
    Optional<Roadmap> findFirstByUserIdOrderByCreatedAtDescIdDesc(UUID userId);
}
