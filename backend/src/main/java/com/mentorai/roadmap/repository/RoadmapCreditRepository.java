package com.mentorai.roadmap.repository;

import com.mentorai.skills.entity.SkillProficiency;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RoadmapCreditRepository {
    public record Evidence(SkillProficiency proficiency,boolean revoked) { }
    private final JdbcTemplate jdbc;
    public RoadmapCreditRepository(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public Map<UUID,Evidence> forRoadmap(UUID roadmapId){
        Map<UUID,Evidence> result=new HashMap<>();
        jdbc.query("select c.task_id,c.proficiency,c.revoked from roadmap_task_credits c join roadmap_tasks t on t.id=c.task_id join roadmap_phases p on p.id=t.phase_id where p.roadmap_id=?",
                (org.springframework.jdbc.core.RowCallbackHandler) r->result.put(r.getObject("task_id",UUID.class),new Evidence(SkillProficiency.valueOf(r.getString("proficiency")),r.getBoolean("revoked"))),roadmapId);
        return result;
    }
    public void retain(UUID taskId,SkillProficiency proficiency){jdbc.update("insert into roadmap_task_credits (task_id,proficiency,revoked) values (?,?,false)",taskId,proficiency.name());}
    public void revoke(UUID taskId,SkillProficiency historicalTarget){
        if(jdbc.update("update roadmap_task_credits set revoked=true where task_id=?",taskId)==0)
            jdbc.update("insert into roadmap_task_credits (task_id,proficiency,revoked) values (?,?,true)",taskId,historicalTarget.name());
    }
}
