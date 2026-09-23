package com.mentorai.mentor;

public interface AiProvider {
    String generate(String systemPolicy,String contextJson);
    boolean enabled();
    String model();
}
