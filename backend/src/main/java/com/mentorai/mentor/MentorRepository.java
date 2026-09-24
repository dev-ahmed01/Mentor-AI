package com.mentorai.mentor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentorai.common.exception.ConflictException;
import com.mentorai.common.exception.ResourceNotFoundException;
import com.mentorai.mentor.MentorModels.*;
import java.sql.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class MentorRepository {
    private final JdbcTemplate jdbc;private final ObjectMapper json;
    public MentorRepository(JdbcTemplate jdbc,ObjectMapper json){this.jdbc=jdbc;this.json=json;}
    public String encode(Object value){try{return json.writeValueAsString(value);}catch(JsonProcessingException e){throw new IllegalStateException("Mentor serialization failed.",e);}}
    private Turn decode(String value){try{return json.readValue(value,Turn.class);}catch(JsonProcessingException e){throw new IllegalStateException("Mentor decoding failed.",e);}}
    private Conversation row(ResultSet r,int n)throws SQLException{return new Conversation(r.getObject("id",UUID.class),r.getObject("career_id",UUID.class),r.getString("career_name"),r.getObject("job_analysis_id",UUID.class),r.getTimestamp("created_at").toInstant(),r.getTimestamp("updated_at").toInstant(),r.getLong("revision"),r.getString("memory"));}
    public Conversation get(UUID id,UUID owner){return jdbc.query("select * from mentor_conversations where id=? and user_id=?",this::row,id,owner).stream().findFirst().orElseThrow(()->new ResourceNotFoundException("Conversation was not found."));}
    public List<Conversation> list(UUID owner){return jdbc.query("select * from mentor_conversations where user_id=? order by updated_at desc,id desc limit 20",this::row,owner);}
    public void create(Conversation c,UUID owner){jdbc.update("insert into mentor_conversations (id,user_id,career_id,career_name,job_analysis_id,created_at,updated_at,revision,memory) values (?,?,?,?,?,?,?,?,?)",c.id(),owner,c.careerId(),c.careerName(),c.jobAnalysisId(),Timestamp.from(c.createdAt()),Timestamp.from(c.updatedAt()),0,"");}
    public List<Turn> turns(UUID id,int limit,int offset){return jdbc.query("select turn_json from mentor_turns where conversation_id=? order by revision desc limit ? offset ?",(r,n)->decode(r.getString(1)),id,limit,offset);}
    public Optional<Turn> request(UUID id,UUID request){return jdbc.query("select turn_json from mentor_turns where conversation_id=? and request_id=?",(r,n)->decode(r.getString(1)),id,request).stream().findFirst();}
    @Transactional
    public Turn append(Conversation c,UUID owner,Turn turn,String memory){
        // Compare-and-swap prevents a slow model response from overwriting newer conversation state.
        if(jdbc.update("update mentor_conversations set revision=revision+1,updated_at=?,memory=? where id=? and user_id=? and revision=? and revision<60",
                Timestamp.from(turn.createdAt()),memory,c.id(),owner,c.revision())!=1)throw new ConflictException("Conversation changed. Reload it before sending again.");
        jdbc.update("insert into mentor_turns (id,conversation_id,request_id,revision,turn_json) values (?,?,?,?,?)",turn.id(),c.id(),turn.requestId(),turn.revision(),encode(turn));
        return turn;
    }
}
