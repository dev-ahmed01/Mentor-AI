package com.mentorai.progress.repository;
import com.mentorai.progress.entity.WeeklyCheckIn; import java.util.Optional; import java.util.UUID; import org.springframework.data.jpa.repository.JpaRepository;
public interface WeeklyCheckInRepository extends JpaRepository<WeeklyCheckIn,UUID>{
    Optional<WeeklyCheckIn> findByPlanIdAndUserId(UUID planId,UUID userId);
    boolean existsByPlanId(UUID planId);
}
