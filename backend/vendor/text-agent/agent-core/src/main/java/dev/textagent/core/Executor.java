package dev.textagent.core;

import com.fasterxml.jackson.databind.JsonNode;

@FunctionalInterface
public interface Executor {
    Result execute(Operation operation, JsonNode arguments, String authorization, String confirmationId);
    record Result(int status, String message) {}
}
