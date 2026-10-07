package dev.textagent.core;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.*;
import java.util.*;

public final class OpenApiCatalog {
    private final Map<String, Operation> operations;
    public OpenApiCatalog(InputStream input, Set<String> allowed) throws IOException {
        JsonNode root = new ObjectMapper(new YAMLFactory()).readTree(input);
        if (root == null || !root.path("openapi").asText().matches("3\\.0\\.[0-9]+")) throw new IllegalArgumentException("Only OpenAPI 3.0.x is supported");
        if (allowed == null || allowed.isEmpty()) throw new IllegalArgumentException("Explicit operation allowlist is required");
        Map<String, Operation> result = new LinkedHashMap<>();
        root.path("paths").fields().forEachRemaining(path -> {
            path.getValue().fields().forEachRemaining(entry -> {
                JsonNode op = entry.getValue(); String id = op.path("operationId").asText();
                if (!allowed.contains(id)) return;
                String method = entry.getKey().toUpperCase(Locale.ROOT);
                if (!Set.of("GET", "POST", "PUT", "PATCH").contains(method)) throw new IllegalArgumentException("Method not allowed: " + method);
                if (!path.getKey().matches("/[a-zA-Z0-9_/{}/-]+") || path.getKey().contains("//")) throw new IllegalArgumentException("Unsupported path");
                if (path.getValue().has("$ref") || op.has("callbacks")) throw new IllegalArgumentException("Unsupported operation construct");
                ObjectNode schema = objectSchema(); ObjectNode props = (ObjectNode) schema.get("properties");
                ArrayNode required = (ArrayNode) schema.get("required");
                ObjectNode pathSchema = objectSchema(), querySchema = objectSchema();
                Map<String, JsonNode> parameters = new LinkedHashMap<>();
                for (JsonNode source : List.of(path.getValue().path("parameters"), op.path("parameters"))) {
                    for (JsonNode raw : source) {
                        JsonNode p = resolve(root, raw, new HashSet<>(), 0);
                        parameters.put(p.path("in").asText() + ":" + p.path("name").asText(), p);
                    }
                }
                for (JsonNode p : parameters.values()) {
                    String location = p.path("in").asText(), name = p.path("name").asText();
                    if (!Set.of("path", "query").contains(location) || !name.matches("[A-Za-z_][A-Za-z0-9_-]*")) throw new IllegalArgumentException("Only named path/query parameters are supported");
                    JsonNode ps = p.path("schema");
                    if (!Set.of("string", "integer", "number", "boolean").contains(ps.path("type").asText()) || p.path("allowReserved").asBoolean()) throw new IllegalArgumentException("Only scalar parameters supported");
                    if (p.has("style") && !p.get("style").asText().equals(location.equals("path") ? "simple" : "form")) throw new IllegalArgumentException("Unsupported parameter style");
                    ObjectNode target = location.equals("path") ? pathSchema : querySchema;
                    ((ObjectNode) target.get("properties")).set(name, ps);
                    if (location.equals("path") || p.path("required").asBoolean()) ((ArrayNode) target.get("required")).add(name);
                }
                var matcher = java.util.regex.Pattern.compile("\\{([^}]+)}").matcher(path.getKey());
                Set<String> placeholders = new HashSet<>(); while (matcher.find()) placeholders.add(matcher.group(1));
                Set<String> pathNames = new HashSet<>(); pathSchema.get("properties").fieldNames().forEachRemaining(pathNames::add);
                if (!placeholders.equals(pathNames)) throw new IllegalArgumentException("Path parameters do not match template");
                props.set("path", pathSchema); props.set("query", querySchema);
                if (!pathNames.isEmpty()) required.add("path");
                if (!querySchema.get("required").isEmpty()) required.add("query");
                if (op.has("requestBody")) {
                    if (method.equals("GET")) throw new IllegalArgumentException("GET body is unsupported");
                    JsonNode body = resolve(root, op.get("requestBody"), new HashSet<>(), 0);
                    if (!body.path("content").has("application/json")) throw new IllegalArgumentException("Only JSON request bodies supported");
                    props.set("body", body.path("content").path("application/json").path("schema"));
                    if (body.path("required").asBoolean()) required.add("body");
                }
                Schema.check(schema);
                Operation operation = new Operation(id, op.path("summary").asText(id), method, path.getKey(), schema);
                if (result.putIfAbsent(id, operation) != null) throw new IllegalArgumentException("Duplicate operationId: " + id);
            });
        });
        if (!result.keySet().equals(allowed)) throw new IllegalArgumentException("Some allowed operations are missing from OpenAPI");
        operations = Collections.unmodifiableMap(result);
    }
    public List<Operation> operations() { return List.copyOf(operations.values()); }
    public Operation require(String id) {
        Operation op = operations.get(id);
        if (op == null) throw new AgentException(422, "Операция не разрешена");
        return op;
    }
    private static ObjectNode objectSchema() {
        ObjectNode o = JsonNodeFactory.instance.objectNode(); o.put("type", "object"); o.putObject("properties"); o.putArray("required"); o.put("additionalProperties", false); return o;
    }
    private static JsonNode resolve(JsonNode root, JsonNode node, Set<String> stack, int depth) {
        if (depth > 40) throw new IllegalArgumentException("Schema nesting too deep");
        if (node.has("$ref")) {
            String ref = node.get("$ref").asText();
            if (!ref.startsWith("#/components/") || node.size() != 1 || !stack.add(ref)) throw new IllegalArgumentException("Only acyclic local component references supported");
            JsonNode target = root.at(ref.substring(1));
            if (target.isMissingNode()) throw new IllegalArgumentException("Unresolved reference: " + ref);
            JsonNode result = resolve(root, target, stack, depth + 1); stack.remove(ref); return result;
        }
        if (node.isObject()) { ObjectNode o = JsonNodeFactory.instance.objectNode(); node.fields().forEachRemaining(f -> o.set(f.getKey(), resolve(root, f.getValue(), stack, depth + 1))); return o; }
        if (node.isArray()) { ArrayNode a = JsonNodeFactory.instance.arrayNode(); node.forEach(v -> a.add(resolve(root, v, stack, depth + 1))); return a; }
        return node.deepCopy();
    }
}
