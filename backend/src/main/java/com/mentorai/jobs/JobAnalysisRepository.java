package com.mentorai.jobs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentorai.common.exception.ResourceNotFoundException;
import com.mentorai.jobs.JobModels.Analysis;
import java.sql.Timestamp;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JobAnalysisRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;
    public JobAnalysisRepository(JdbcTemplate jdbc,ObjectMapper json) { this.jdbc=jdbc;this.json=json; }
    public void save(UUID owner,Analysis result) {
        try {
            jdbc.update("insert into job_analyses (id,user_id,calculated_at,calculation_version,analysis_json) values (?,?,?,?,?)",
                    result.id(),owner,Timestamp.from(result.calculatedAt()),result.calculationVersion(),json.writeValueAsString(result));
        } catch(JsonProcessingException e) { throw new IllegalStateException("Job analysis serialization failed.",e); }
    }
    public Analysis get(UUID id,UUID owner) {
        String value=jdbc.query("select analysis_json from job_analyses where id=? and user_id=?",(r,n)->r.getString(1),id,owner)
                .stream().findFirst().orElseThrow(()->new ResourceNotFoundException("Job analysis was not found."));
        try { return json.readValue(value,Analysis.class); }
        catch(JsonProcessingException e) { throw new IllegalStateException("Job analysis decoding failed.",e); }
    }
}
