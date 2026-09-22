package com.mentorai.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentorai.market.MarketModels.RawJob;
import java.net.URI;
import java.net.http.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class ArbeitnowProvider implements MarketDataProvider {
    public static final String SOURCE="ARBEITNOW";
    public static final String URL="https://www.arbeitnow.com/api/job-board-api";
    private final ObjectMapper json;
    private final HttpClient client;
    @org.springframework.beans.factory.annotation.Autowired
    public ArbeitnowProvider(ObjectMapper json) {
        this(json,HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).followRedirects(HttpClient.Redirect.NEVER).build());
    }
    ArbeitnowProvider(ObjectMapper json,HttpClient client) { this.json=json;this.client=client; }

    @Override public List<RawJob> fetch() throws Exception {
        var request=HttpRequest.newBuilder(URI.create(URL)).timeout(Duration.ofSeconds(20))
                .header("Accept","application/json").header("User-Agent","MentorAI-Hackathon/0.1 (bounded evidence collector)").GET().build();
        // A bounded subscriber prevents a malicious/accidental oversized body from being buffered.
        var response=client.send(request, ignored -> new LimitedBodySubscriber(5_000_000));
        if(response.statusCode()!=200) throw new IllegalStateException("Provider request failed: "+response.statusCode());
        var data=json.readTree(response.body()).path("data");
        if(!data.isArray()) throw new IllegalStateException("Unexpected provider schema.");
        List<RawJob> result=new ArrayList<>();
        for(int index=0;index<Math.min(100,data.size());index++) {
            var item=data.get(index);
            Instant published=null;
            try { if(item.path("created_at").isIntegralNumber())published=Instant.ofEpochSecond(item.path("created_at").asLong()); }
            catch(DateTimeException ignored) { }
            result.add(new RawJob(item.path("slug").asText(),item.path("url").asText(),item.path("title").asText(),
                    item.path("company_name").asText(),item.path("location").asText(),item.path("remote").asBoolean(),
                    published,item.path("description").asText(),item.toString()));
        }
        return List.copyOf(result);
    }

    private static final class LimitedBodySubscriber implements HttpResponse.BodySubscriber<byte[]> {
        private final HttpResponse.BodySubscriber<byte[]> delegate=HttpResponse.BodySubscribers.ofByteArray();
        private final int limit;
        private int bytes;
        private java.util.concurrent.Flow.Subscription subscription;
        LimitedBodySubscriber(int limit) { this.limit=limit; }
        public java.util.concurrent.CompletionStage<byte[]> getBody() { return delegate.getBody(); }
        public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) { this.subscription=subscription; delegate.onSubscribe(subscription); }
        public void onNext(List<java.nio.ByteBuffer> buffers) {
            for(var buffer:buffers)bytes+=buffer.remaining();
            if(bytes>limit) { subscription.cancel(); delegate.onError(new IllegalStateException("Provider response exceeded size limit.")); }
            else delegate.onNext(buffers);
        }
        public void onError(Throwable error) { delegate.onError(error); }
        public void onComplete() { delegate.onComplete(); }
    }
}
