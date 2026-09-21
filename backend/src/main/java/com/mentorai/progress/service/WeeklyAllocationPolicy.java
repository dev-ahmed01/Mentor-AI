package com.mentorai.progress.service;
import com.mentorai.decision.service.LearningPriorityPolicy; import com.mentorai.roadmap.dto.RoadmapResponse;
import com.mentorai.roadmap.entity.TaskState; import java.util.ArrayList; import java.util.List; import java.util.Set; import java.util.UUID;
import org.springframework.stereotype.Component;
@Component
public class WeeklyAllocationPolicy {
    public record Allocation(UUID taskId,String title,int hours) { }
    private final LearningPriorityPolicy priorities;
    public WeeklyAllocationPolicy(LearningPriorityPolicy priorities){this.priorities=priorities;}
    public List<Allocation> allocate(RoadmapResponse roadmap,int capacity,Set<UUID> excluded){
        if(capacity==0)return List.of();
        var eligible=roadmap.phases().stream().flatMap(p->p.tasks().stream())
                .filter(t->!t.state().terminal()&&t.ready()&&!excluded.contains(t.id()))
                .sorted(java.util.Comparator.comparingInt(t->t.state()==TaskState.IN_PROGRESS?0:1)).toList();
        List<Allocation> result=new ArrayList<>(); int remaining=capacity;
        for(var task:eligible){if(remaining==0||result.size()>=priorities.focusSlots(capacity))break;
            int hours=Math.min(remaining,task.estimatedHours()); result.add(new Allocation(task.id(),task.title(),hours)); remaining-=hours;}
        return List.copyOf(result);
    }
}
