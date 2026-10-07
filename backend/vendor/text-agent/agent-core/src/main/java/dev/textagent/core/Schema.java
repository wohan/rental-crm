package dev.textagent.core;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.regex.Pattern;

/** Deliberately restricted OpenAPI 3.0 schema profile. Unsupported keywords fail at startup. */
public final class Schema {
    private Schema() {}
    private static final Set<String> KEYS = Set.of("type", "properties", "required", "additionalProperties", "items",
        "enum", "nullable", "minLength", "maxLength", "pattern", "minimum", "maximum", "exclusiveMinimum",
        "exclusiveMaximum", "minItems", "maxItems", "uniqueItems", "format", "title", "description", "example", "default");
    public static void check(JsonNode s) {
        if (!s.isObject()) throw new IllegalArgumentException("Schema must be an object");
        s.fieldNames().forEachRemaining(k -> { if (!KEYS.contains(k)) throw new IllegalArgumentException("Unsupported schema keyword: " + k); });
        String type = s.path("type").asText();
        if (!Set.of("object", "array", "string", "integer", "number", "boolean").contains(type))
            throw new IllegalArgumentException("Explicit supported schema type required");
        if (s.has("format") && !Set.of("date", "date-time", "uuid", "email", "int32", "int64", "float", "double").contains(s.get("format").asText()))
            throw new IllegalArgumentException("Unsupported format: " + s.get("format"));
        if (s.has("pattern")) Pattern.compile(s.get("pattern").asText());
        if (type.equals("object")) {
            if (!s.path("properties").isObject() || !s.has("additionalProperties") || !s.get("additionalProperties").isBoolean() || s.get("additionalProperties").asBoolean())
                throw new IllegalArgumentException("Objects require properties and additionalProperties: false");
            s.get("properties").forEach(Schema::check);
            if (s.has("required") && !s.get("required").isArray()) throw new IllegalArgumentException("required must be an array");
            for (JsonNode key : s.path("required")) if (!key.isTextual() || !s.get("properties").has(key.asText()))
                throw new IllegalArgumentException("Required property not defined: " + key);
        }
        if (type.equals("array")) check(s.path("items"));
    }
    public static List<String> validate(JsonNode schema, JsonNode value) {
        List<String> errors = new ArrayList<>();
        validate(schema, value, "$", errors);
        return List.copyOf(errors);
    }
    private static void validate(JsonNode s, JsonNode v, String p, List<String> e) {
        if (v == null || v.isMissingNode()) { e.add(p + ": значение отсутствует"); return; }
        if (v.isNull() && s.path("nullable").asBoolean()) return;
        String t = s.path("type").asText();
        boolean validType = switch (t) {
            case "object" -> v.isObject(); case "array" -> v.isArray(); case "string" -> v.isTextual();
            case "integer" -> v.isIntegralNumber(); case "number" -> v.isNumber(); case "boolean" -> v.isBoolean(); default -> false;
        };
        if (!validType) { e.add(p + ": ожидается " + t); return; }
        if (s.has("enum")) { boolean found = false; for (JsonNode item : s.get("enum")) found |= item.equals(v); if (!found) e.add(p + ": недопустимое значение"); }
        if (v.isObject()) {
            for (JsonNode key : s.path("required")) if (!v.has(key.asText())) e.add(p + "." + key.asText() + ": обязательное поле");
            v.fields().forEachRemaining(f -> {
                JsonNode child = s.path("properties").get(f.getKey());
                if (child == null) e.add(p + "." + f.getKey() + ": неизвестное поле");
                else validate(child, f.getValue(), p + "." + f.getKey(), e);
            });
        }
        if (v.isArray()) {
            if (s.has("minItems") && v.size() < s.get("minItems").asInt()) e.add(p + ": слишком мало элементов");
            if (s.has("maxItems") && v.size() > s.get("maxItems").asInt()) e.add(p + ": слишком много элементов");
            Set<JsonNode> seen = new HashSet<>();
            for (int i = 0; i < v.size(); i++) {
                validate(s.get("items"), v.get(i), p + "[" + i + "]", e);
                if (s.path("uniqueItems").asBoolean() && !seen.add(v.get(i))) e.add(p + ": повторяющиеся элементы");
            }
        }
        if (v.isNumber()) {
            if (s.has("minimum")) { int c = v.decimalValue().compareTo(s.get("minimum").decimalValue()); if (c < 0 || c == 0 && s.path("exclusiveMinimum").asBoolean()) e.add(p + ": ниже минимума"); }
            if (s.has("maximum")) { int c = v.decimalValue().compareTo(s.get("maximum").decimalValue()); if (c > 0 || c == 0 && s.path("exclusiveMaximum").asBoolean()) e.add(p + ": выше максимума"); }
            if (s.path("format").asText().equals("int32") && !v.canConvertToInt()) e.add(p + ": вне диапазона int32");
            if (s.path("format").asText().equals("int64") && !v.canConvertToLong()) e.add(p + ": вне диапазона int64");
        }
        if (v.isTextual()) {
            String text = v.asText(); int length = text.codePointCount(0, text.length());
            if (s.has("minLength") && length < s.get("minLength").asInt()) e.add(p + ": слишком короткое значение");
            if (s.has("maxLength") && length > s.get("maxLength").asInt()) e.add(p + ": слишком длинное значение");
            if (s.has("pattern") && !Pattern.compile(s.get("pattern").asText()).matcher(text).find()) e.add(p + ": неверный формат");
            try {
                switch (s.path("format").asText()) {
                    case "date" -> LocalDate.parse(text);
                    case "date-time" -> OffsetDateTime.parse(text);
                    case "uuid" -> { if (!UUID.fromString(text).toString().equalsIgnoreCase(text)) throw new IllegalArgumentException(); }
                    case "email" -> { if (!text.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) throw new IllegalArgumentException(); }
                    default -> { }
                }
            } catch (Exception ex) { e.add(p + ": неверный формат " + s.path("format").asText()); }
        }
    }
}
