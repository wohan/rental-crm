package dev.textagent.core;

import com.fasterxml.jackson.databind.JsonNode;

public record Operation(String id, String description, String method, String path, JsonNode schema) {
    public Operation { schema = schema.deepCopy(); }
    @Override public JsonNode schema() { return schema.deepCopy(); }
}
