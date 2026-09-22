package com.mentorai.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentorai.common.exception.ResourceNotFoundException;
import com.mentorai.market.MarketModels.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MarketRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;
    public MarketRepository(JdbcTemplate jdbc,ObjectMapper json) { this.jdbc=jdbc; this.json=json; }
    public String encode(Object value) {
        try { return json.writeValueAsString(value); } catch(Exception e) { throw new IllegalStateException("Evidence serialization failed.",e); }
    }
    public <T> T decode(String value,Class<T> type) {
        try { return json.readValue(value,type); } catch(Exception e) { throw new IllegalStateException("Evidence decoding failed.",e); }
    }
    public boolean claim(Instant now) {
        return jdbc.update("update market_sources set last_attempt_at=?, last_status='COLLECTING' where source=? and (last_attempt_at is null or last_attempt_at<=?)",
                Timestamp.from(now),ArbeitnowProvider.SOURCE,Timestamp.from(now.minusSeconds(21600)))==1;
    }
    public void succeeded(Instant now) { jdbc.update("update market_sources set last_success_at=?,last_status='SUCCESS' where source=?",Timestamp.from(now),ArbeitnowProvider.SOURCE); }
    public void failed() { jdbc.update("update market_sources set last_status='FAILED' where source=?",ArbeitnowProvider.SOURCE); }
    public SourceState source() {
        return jdbc.queryForObject("select * from market_sources where source=?",(r,n)->new SourceState(r.getString("source"),
                r.getTimestamp("last_attempt_at")==null?null:r.getTimestamp("last_attempt_at").toInstant(),
                r.getTimestamp("last_success_at")==null?null:r.getTimestamp("last_success_at").toInstant(),r.getString("last_status")),ArbeitnowProvider.SOURCE);
    }
    public List<UUID> controlledSkillIds() {
        return jdbc.queryForList("select skill_id from career_skills union select skill_id from skill_dependencies union select prerequisite_skill_id from skill_dependencies",UUID.class);
    }
    public Observation store(Observation observation,String hash,String raw) {
        var existing=jdbc.query("select normalized_json from market_observations where source=? and source_id=? and content_hash=?",
                (r,n)->decode(r.getString(1),Observation.class),observation.source(),observation.sourceId(),hash);
        if(!existing.isEmpty())return existing.getFirst();
        jdbc.update("insert into market_observations (id,source,source_id,content_hash,source_url,collected_at,published_at,processing_version,raw_payload,normalized_json) values (?,?,?,?,?,?,?,?,?,?)",
                observation.id(),observation.source(),observation.sourceId(),hash,observation.sourceUrl(),Timestamp.from(observation.collectedAt()),
                Timestamp.from(observation.publishedAt()),observation.processingVersion(),raw,encode(observation));
        return observation;
    }
    public void snapshot(Snapshot snapshot) {
        jdbc.update("insert into market_snapshots (id,career_id,collected_at,processing_version,snapshot_json) values (?,?,?,?,?)",
                snapshot.id(),snapshot.careerId(),Timestamp.from(snapshot.collectedAt()),snapshot.processingVersion(),encode(snapshot));
        for(var item:snapshot.observations())jdbc.update("insert into market_snapshot_observations values (?,?)",snapshot.id(),item.id());
    }
    public Optional<Snapshot> latest(UUID careerId) {
        return jdbc.query("select snapshot_json from market_snapshots where career_id=? order by collected_at desc,id desc limit 1",
                (r,n)->decode(r.getString(1),Snapshot.class),careerId).stream().findFirst();
    }
    public Snapshot snapshot(UUID id) {
        return jdbc.query("select snapshot_json from market_snapshots where id=?",(r,n)->decode(r.getString(1),Snapshot.class),id)
                .stream().findFirst().orElseThrow(()->new ResourceNotFoundException("Market snapshot was not found."));
    }
    public Observation observation(UUID id) {
        return jdbc.query("select normalized_json from market_observations where id=?",(r,n)->decode(r.getString(1),Observation.class),id)
                .stream().findFirst().orElseThrow(()->new ResourceNotFoundException("Market observation was not found."));
    }
    public void decision(UUID id,UUID owner,UUID snapshot,Instant at,String version,Object value) {
        jdbc.update("insert into market_decisions values (?,?,?,?,?,?)",id,owner,snapshot,Timestamp.from(at),version,encode(value));
    }
    public String decision(UUID id,UUID owner) {
        return jdbc.query("select decision_json from market_decisions where id=? and user_id=?",(r,n)->r.getString(1),id,owner)
                .stream().findFirst().orElseThrow(()->new ResourceNotFoundException("Market decision was not found."));
    }
}
