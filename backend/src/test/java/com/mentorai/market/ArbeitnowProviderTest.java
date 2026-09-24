package com.mentorai.market;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Flow;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

class ArbeitnowProviderTest {
    @Test void acceptsNormalLargePagesWithinFiveMegabyteBound() throws Exception {
        var json=new ObjectMapper();
        List<Map<String,Object>> rows=new ArrayList<>();
        for(int i=0;i<100;i++)rows.add(Map.of("slug","fixture-"+i,"url","https://www.arbeitnow.com/jobs/fixture-"+i,
                "title","Backend Developer","company_name","DEMO Company "+i,"location","Berlin","remote",false,
                "created_at",1700000000,"description","Java. ".repeat(4500)));
        byte[] body=json.writeValueAsBytes(Map.of("data",rows));
        assertThat(body.length).isBetween(2_000_001,5_000_000);
        assertThat(provider(body).fetch()).hasSize(100);
    }

    @Test void oversizedResponseIsCancelledBeforeParsing() throws Exception {
        byte[] body="x".repeat(5_000_001).getBytes(StandardCharsets.UTF_8);
        assertThatThrownBy(()->provider(body).fetch()).hasRootCauseMessage("Provider response exceeded size limit.");
    }

    @Test void largerSourcePagesAreLimitedToFirstHundredRecords() throws Exception {
        var rows=new ArrayList<Map<String,Object>>();
        for(int i=0;i<250;i++)rows.add(Map.of("slug","fixture-"+i,"created_at",1700000000,"description","Java"));
        var result=provider(new ObjectMapper().writeValueAsBytes(Map.of("data",rows))).fetch();
        assertThat(result).hasSize(100);
        assertThat(result.getFirst().sourceId()).isEqualTo("fixture-0");
        assertThat(result.getLast().sourceId()).isEqualTo("fixture-99");
    }

    @SuppressWarnings("unchecked")
    private ArbeitnowProvider provider(byte[] body) throws Exception {
        HttpClient client=mock(HttpClient.class);
        when(client.send(any(HttpRequest.class),ArgumentMatchers.<HttpResponse.BodyHandler<byte[]>>any())).thenAnswer(invocation->{
            HttpRequest request=invocation.getArgument(0);
            assertThat(request.uri().toString()).isEqualTo(ArbeitnowProvider.URL);
            assertThat(request.timeout()).contains(java.time.Duration.ofSeconds(20));
            HttpResponse.BodyHandler<byte[]> handler=invocation.getArgument(1);
            var subscriber=handler.apply(mock(HttpResponse.ResponseInfo.class));
            var subscription=mock(Flow.Subscription.class);
            subscriber.onSubscribe(subscription);
            subscriber.onNext(List.of(ByteBuffer.wrap(body)));
            subscriber.onComplete();
            byte[] result=subscriber.getBody().toCompletableFuture().join();
            HttpResponse<byte[]> response=mock(HttpResponse.class);
            when(response.statusCode()).thenReturn(200);when(response.body()).thenReturn(result);
            return response;
        });
        return new ArbeitnowProvider(new ObjectMapper(),client);
    }
}
