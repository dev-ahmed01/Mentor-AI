package com.mentorai.mentor;

import java.net.URI;
import java.io.*;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OllamaAiProvider implements AiProvider {
    private final boolean enabled;
    private final String modelName;
    private final OllamaChatModel client;
    public OllamaAiProvider(@Value("${mentorai.ai.enabled:false}") boolean enabled,
                            @Value("${mentorai.ai.base-url:http://localhost:11434}") String baseUrl,
                            @Value("${mentorai.ai.model:qwen3:8b}") String model) {
        this.enabled=enabled;this.modelName=model;
        if(!enabled){client=null;return;}
        URI uri=URI.create(baseUrl);
        if(!"http".equals(uri.getScheme())||!List.of("localhost","127.0.0.1","[::1]").contains(uri.getHost())
                ||uri.getUserInfo()!=null||uri.getQuery()!=null||uri.getFragment()!=null||(!uri.getPath().isEmpty()&&!uri.getPath().equals("/")))
            throw new IllegalArgumentException("Mentor Ollama endpoint must be a local HTTP origin.");
        var factory=new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).followRedirects(HttpClient.Redirect.NEVER).build());
        factory.setReadTimeout(Duration.ofSeconds(45));
        var rest=RestClient.builder().requestFactory(factory).requestInterceptor((request,body,execution)->{
            var response=execution.execute(request,body);
            return new ClientHttpResponse(){
                private InputStream limited;
                public HttpStatusCode getStatusCode()throws IOException{return response.getStatusCode();}
                public String getStatusText()throws IOException{return response.getStatusText();}
                public HttpHeaders getHeaders(){return response.getHeaders();}
                public void close(){response.close();}
                public InputStream getBody()throws IOException{
                    if(limited==null)limited=new FilterInputStream(response.getBody()){
                        private int count;
                        private void used(int n)throws IOException{if(n>0&&(count+=n)>262144)throw new IOException("AI response exceeds byte limit.");}
                        public int read()throws IOException{int value=in.read();used(value<0?0:1);return value;}
                        public int read(byte[] buffer,int offset,int length)throws IOException{int n=in.read(buffer,offset,length);used(n);return n;}
                    };
                    return limited;
                }
            };
        });
        var api=OllamaApi.builder().baseUrl(baseUrl).restClientBuilder(rest).build();
        client=OllamaChatModel.builder().ollamaApi(api).retryTemplate(RetryTemplate.builder().maxAttempts(1).build())
                .defaultOptions(OllamaChatOptions.builder().model(model).temperature(0.0).format("json").numCtx(8192).numPredict(256).internalToolExecutionEnabled(false).build()).build();
    }
    public boolean enabled(){return enabled;}
    public String model(){return modelName;}
    public String generate(String policy,String data) {
        if(!enabled)throw new IllegalStateException("AI provider disabled.");
        return client.call(new Prompt(List.of(new SystemMessage(policy),new UserMessage(data)))).getResult().getOutput().getText();
    }
}
