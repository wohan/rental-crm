package dev.textagent.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.net.URI;
import java.time.Duration;
import java.util.Set;

@ConfigurationProperties("text-agent")
public record AgentProperties(boolean enabled, String spec, Set<String> allowedOperations, URI targetBaseUrl,
                              URI modelEndpoint, String model, String apiKey, java.util.Map<String, Object> modelOptions, Duration confirmationTtl,
                              Duration sessionTtl, Integer maxSessions) {
    public AgentProperties {
        if (modelOptions == null) modelOptions = java.util.Map.of();
        if (confirmationTtl == null) confirmationTtl = Duration.ofMinutes(3);
        if (sessionTtl == null) sessionTtl = Duration.ofMinutes(30);
        if (maxSessions == null) maxSessions = 1000;
    }
}
