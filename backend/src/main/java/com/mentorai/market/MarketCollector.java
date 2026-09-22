package com.mentorai.market;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration @EnableScheduling
public class MarketCollector implements ApplicationRunner {
    private final MarketIngestionService ingestion;
    private final boolean enabled;
    private final boolean onStart;
    public MarketCollector(MarketIngestionService ingestion,@Value("${mentorai.market.enabled:false}") boolean enabled,
                           @Value("${mentorai.market.refresh-on-start:false}") boolean onStart) {
        this.ingestion=ingestion;this.enabled=enabled;this.onStart=onStart;
    }
    public void run(ApplicationArguments args) { if(enabled && onStart)collect(); }
    @Scheduled(initialDelay=21600000,fixedDelay=21600000)
    public void scheduled() { if(enabled)collect(); }
    private void collect() { LoggerFactory.getLogger(getClass()).info("Market collection result: {}",ingestion.refresh()); }
}
