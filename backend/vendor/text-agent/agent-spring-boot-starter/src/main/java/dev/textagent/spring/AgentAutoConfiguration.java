package dev.textagent.spring;

import dev.textagent.core.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;
import java.time.Clock;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "text-agent", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(AgentProperties.class)
public class AgentAutoConfiguration {
    @Bean @ConditionalOnMissingBean
    OpenApiCatalog agentCatalog(AgentProperties p, ResourceLoader resources) throws java.io.IOException {
        if (p.spec() == null || !(p.spec().startsWith("classpath:") || p.spec().startsWith("file:")))
            throw new IllegalArgumentException("text-agent.spec must be classpath: or file:");
        try (var input = resources.getResource(p.spec()).getInputStream()) { return new OpenApiCatalog(input, p.allowedOperations()); }
    }
    @Bean @ConditionalOnMissingBean(Planner.class)
    Planner agentPlanner(AgentProperties p) { return new ChatCompletionPlanner(p.modelEndpoint(), p.model(), p.apiKey(), p.modelOptions()); }
    @Bean @ConditionalOnMissingBean(dev.textagent.core.Executor.class)
    dev.textagent.core.Executor agentExecutor(AgentProperties p) { return new HttpExecutor(p.targetBaseUrl()); }
    @Bean @ConditionalOnMissingBean
    AgentEngine agentEngine(OpenApiCatalog catalog, Planner planner, dev.textagent.core.Executor executor, AgentProperties p) {
        return new AgentEngine(catalog, planner, executor, Clock.systemUTC(), p.confirmationTtl(), p.sessionTtl(), p.maxSessions());
    }
    @Bean @ConditionalOnMissingBean
    AgentController agentController(AgentEngine engine) { return new AgentController(engine); }
}
