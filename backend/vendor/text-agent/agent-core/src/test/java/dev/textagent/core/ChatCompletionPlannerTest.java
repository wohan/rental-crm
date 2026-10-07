package dev.textagent.core;

import com.fasterxml.jackson.databind.*;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import java.net.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import static org.junit.jupiter.api.Assertions.*;

class ChatCompletionPlannerTest {
    @Test void textThroughModelAndConfirmationToRealHttp() throws Exception {
        var json = new ObjectMapper(); var calls = new AtomicInteger(); var requestBody = new AtomicReference<JsonNode>();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/chat/completions", ex -> {
            String request = new String(ex.getRequestBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            assertTrue(request.contains("createTenant")); assertFalse(request.contains("Bearer end-user"));
            var proposal = json.createObjectNode().put("operationId", "createTenant");
            proposal.putObject("arguments").putObject("body").put("fullName", "Мария Иванова").put("phone", "+79001112233");
            var result = json.createObjectNode(); var choice = result.putArray("choices").addObject(); choice.put("finish_reason", "stop");
            choice.putObject("message").put("content", proposal.toString());
            byte[] bytes = json.writeValueAsBytes(result); ex.sendResponseHeaders(200, bytes.length); ex.getResponseBody().write(bytes); ex.close();
        });
        server.createContext("/api/tenants", ex -> {
            calls.incrementAndGet(); requestBody.set(json.readTree(ex.getRequestBody()));
            assertEquals("Bearer end-user", ex.getRequestHeaders().getFirst("Authorization"));
            ex.sendResponseHeaders(200, -1); ex.close();
        }); server.start();
        try {
            URI base = URI.create("http://127.0.0.1:" + server.getAddress().getPort());
            var catalog = new OpenApiCatalog(getClass().getResourceAsStream("/agent-openapi.yaml"), Set.of("createTenant"));
            var engine = new AgentEngine(catalog, new ChatCompletionPlanner(base.resolve("/chat/completions"), "test-model", "test-provider-key"),
                new HttpExecutor(base), Clock.systemUTC(), Duration.ofMinutes(3), Duration.ofMinutes(30), 10);
            var preview = engine.message("alice", null, "Создай арендатора Марию Иванову, телефон +79001112233");
            assertEquals("READY_FOR_CONFIRMATION", preview.state()); assertEquals(0, calls.get());
            assertEquals("DONE", engine.confirm("alice", preview.sessionId(), preview.confirmationId(), "Bearer end-user").state());
            assertEquals(preview.arguments().get("body"), requestBody.get());
            engine.confirm("alice", preview.sessionId(), preview.confirmationId(), "Bearer end-user"); assertEquals(1, calls.get());
        } finally { server.stop(0); }
    }
}
