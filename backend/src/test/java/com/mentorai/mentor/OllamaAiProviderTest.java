package com.mentorai.mentor;

import static org.assertj.core.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.*;
import org.junit.jupiter.api.Test;

class OllamaAiProviderTest {
    @Test void springAiSendsSeparatedMessagesAndDoesNotRetryTransportFailure()throws Exception{
        var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);var json=new ObjectMapper();var captured=new AtomicReference<String>();var calls=new AtomicInteger();
        server.createContext("/api/chat",exchange->{calls.incrementAndGet();captured.set(new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8));
            byte[] body=json.writeValueAsBytes(Map.of("model","fixture","message",Map.of("role","assistant","content","{\"factIds\":[\"profile\"],\"nextStep\":\"REVIEW_PROFILE\"}"),"done",true));
            exchange.getResponseHeaders().set("Content-Type","application/json");exchange.sendResponseHeaders(200,body.length);exchange.getResponseBody().write(body);exchange.close();});
        server.start();
        try{
            var provider=new OllamaAiProvider(true,"http://127.0.0.1:"+server.getAddress().getPort(),"fixture");
            assertThat(provider.generate("SYSTEM POLICY","untrusted question")).contains("factIds");
            var request=json.readTree(captured.get());assertThat(request.path("messages").get(0).path("role").asText()).isEqualTo("system");assertThat(request.path("messages").get(1).path("role").asText()).isEqualTo("user");assertThat(request.path("stream").asBoolean()).isFalse();assertThat(request.path("options").path("num_predict").asInt()).isEqualTo(256);
            server.removeContext("/api/chat");server.createContext("/api/chat",exchange->{calls.incrementAndGet();exchange.sendResponseHeaders(503,-1);exchange.close();});
            assertThatThrownBy(()->provider.generate("policy","data")).isInstanceOf(RuntimeException.class);assertThat(calls.get()).isEqualTo(2);
        }finally{server.stop(0);}
    }
    @Test void providerRejectsNonLocalOriginsAndDisabledProviderNeedsNoServer(){
        assertThatThrownBy(()->new OllamaAiProvider(true,"https://example.com","fixture")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->new OllamaAiProvider(true,"http://localhost:11434/path","fixture")).isInstanceOf(IllegalArgumentException.class);
        var provider=new OllamaAiProvider(false,"http://localhost:11434","fixture");assertThat(provider.enabled()).isFalse();assertThatThrownBy(()->provider.generate("policy","data")).isInstanceOf(IllegalStateException.class);
    }
    @Test void oversizedProviderBodyIsRejected()throws Exception{
        var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        byte[] body=("{\"model\":\"fixture\",\"message\":{\"role\":\"assistant\",\"content\":\""+"x".repeat(300000)+"\"},\"done\":true}").getBytes(StandardCharsets.UTF_8);
        server.createContext("/api/chat",exchange->{exchange.getResponseHeaders().set("Content-Type","application/json");exchange.sendResponseHeaders(200,body.length);exchange.getResponseBody().write(body);exchange.close();});server.start();
        try{var provider=new OllamaAiProvider(true,"http://127.0.0.1:"+server.getAddress().getPort(),"fixture");assertThatThrownBy(()->provider.generate("policy","data")).isInstanceOf(RuntimeException.class);}finally{server.stop(0);}
    }
}
