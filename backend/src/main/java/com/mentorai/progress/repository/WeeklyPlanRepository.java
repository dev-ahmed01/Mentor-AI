package com.mentorai.progress.repository;
import com.mentorai.progress.entity.WeeklyPlan; import java.time.LocalDate; import java.util.Optional; import java.util.UUID;
import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable; import org.springframework.data.jpa.repository.JpaRepository;
public interface WeeklyPlanRepository extends JpaRepository<WeeklyPlan,UUID>{
    Optional<WeeklyPlan> findByIdAndUserId(UUID id,UUID userId);
    Optional<WeeklyPlan> findByUserIdAndWeekStart(UUID userId,LocalDate weekStart);
    Page<WeeklyPlan> findByUserIdOrderByWeekStartDesc(UUID userId,Pageable pageable);
}
