package com.mentorai.pivot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentorai.common.exception.ResourceNotFoundException;
import java.sql.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import static com.mentorai.pivot.PivotModels.*;

@Repository
public class PivotRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;
    public PivotRepository(JdbcTemplate jdbc,ObjectMapper json){this.jdbc=jdbc;this.json=json;}
    public String encode(Object value){try{return json.writeValueAsString(value);}catch(JsonProcessingException e){throw new IllegalStateException("Could not save pivot snapshot.",e);}}
    private Comparison decode(String value){try{return json.readValue(value,Comparison.class);}catch(JsonProcessingException e){throw new IllegalStateException("Could not read pivot snapshot.",e);}}
    private Stored row(ResultSet r,int n)throws SQLException {
        UUID accepted=r.getObject("accepted_roadmap_id",UUID.class);Timestamp at=r.getTimestamp("accepted_at");
        return new Stored(new Detail(r.getObject("id",UUID.class),r.getTimestamp("created_at").toInstant(),accepted==null?"PREVIEW":"ACCEPTED",accepted,at==null?null:at.toInstant(),decode(r.getString("comparison_json"))),r.getString("profile_fingerprint"));
    }
    public Stored get(UUID id,UUID owner){return jdbc.query("select * from career_pivots where id=? and user_id=?",this::row,id,owner).stream().findFirst().orElseThrow(()->new ResourceNotFoundException("Career pivot was not found."));}
    public List<Summary> list(UUID owner){return jdbc.query("select * from career_pivots where user_id=? order by created_at desc,id desc limit 20",this::row,owner).stream().map(Stored::detail).map(d->new Summary(d.id(),d.createdAt(),d.comparison().sourceRoadmap().careerName(),d.comparison().after().careerName(),d.status(),d.acceptedRoadmapId())).toList();}
    public void create(UUID owner,Detail detail,String fingerprint){jdbc.update("insert into career_pivots (id,user_id,source_roadmap_id,target_career_id,created_at,comparison_json,profile_fingerprint) values (?,?,?,?,?,?,?)",detail.id(),owner,detail.comparison().sourceRoadmap().id(),detail.comparison().after().careerId(),Timestamp.from(detail.createdAt()),encode(detail.comparison()),fingerprint);}
    public void accept(UUID id,UUID owner,UUID roadmap,java.time.Instant at){
        if(jdbc.update("update career_pivots set accepted_roadmap_id=?,accepted_at=? where id=? and user_id=? and accepted_roadmap_id is null",roadmap,Timestamp.from(at),id,owner)!=1)throw new IllegalStateException("Pivot acceptance was not recorded.");
    }
}
