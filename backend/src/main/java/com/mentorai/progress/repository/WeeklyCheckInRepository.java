package com.mentorai.progress.repository;
import com.mentorai.progress.entity.WeeklyCheckIn; import java.util.Optional; import java.util.UUID; import org.springframework.data.jpa.repository.JpaRepository;
public interface WeeklyCheckInRepository extends JpaRepository<WeeklyCheckIn,UUID>{
    Optional<WeeklyCheckIn> findByPlanIdAndUserId(UUID planId,UUID userId);
    boolean existsByPlanId(UUID planId);
    @org.springframework.data.jpa.repository.Query("select c from WeeklyCheckIn c, WeeklyPlan p where c.planId = p.id and c.userId = :owner and c.roadmapId = :roadmap and p.weekStart <= :through order by p.weekStart asc")
    java.util.List<WeeklyCheckIn> historyThrough(UUID owner, UUID roadmap, java.time.LocalDate through);
}
