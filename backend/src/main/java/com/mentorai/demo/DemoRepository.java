package com.mentorai.demo;

import java.sql.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import static com.mentorai.demo.DemoModels.*;

@Repository
public class DemoRepository {
    private final JdbcTemplate jdbc;
    public DemoRepository(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public Optional<Run> get(UUID owner){return jdbc.query("select * from demo_runs where user_id=?",(r,n)->new Run(r.getObject("id",UUID.class),r.getObject("roadmap_id",UUID.class),r.getObject("plan_id",UUID.class),r.getDate("week_start").toLocalDate(),r.getLong("roadmap_revision"),r.getLong("plan_revision"),r.getString("scenario_version"),r.getTimestamp("created_at").toInstant(),r.getObject("exam_check_in_id",UUID.class)),owner).stream().findFirst();}
    public boolean hasHistory(UUID owner){
        // Roots cover all user-owned feature history; dependent rows reference these roots.
        for(String table:List.of("roadmaps","weekly_plans","mentor_conversations","market_decisions","job_analyses","career_pivots"))
            if(jdbc.queryForObject("select count(*) from "+table+" where user_id=?",Long.class,owner)>0)return true;
        return false;
    }
    public void save(UUID owner,Run run){jdbc.update("insert into demo_runs (id,user_id,roadmap_id,plan_id,week_start,roadmap_revision,plan_revision,scenario_version,created_at) values (?,?,?,?,?,?,?,?,?)",run.id(),owner,run.roadmapId(),run.planId(),java.sql.Date.valueOf(run.weekStart()),run.roadmapRevision(),run.planRevision(),run.scenarioVersion(),Timestamp.from(run.createdAt()));}
    public void recordExam(UUID owner,UUID checkIn){jdbc.update("update demo_runs set exam_check_in_id=? where user_id=?",checkIn,owner);}
}
