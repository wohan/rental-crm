package dev.textagent.core;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
class OpenApiCatalogTest {
    String spec() throws IOException { try (var in = getClass().getResourceAsStream("/agent-openapi.yaml")) { return new String(in.readAllBytes(), StandardCharsets.UTF_8); } }
    OpenApiCatalog load(String text, Set<String> allow) throws IOException { return new OpenApiCatalog(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)), allow); }
    @Test void loadsLocalRefAndOnlyAllowedOperations() throws Exception { assertEquals(1, load(spec(), Set.of("createTenant")).operations().size()); }
    @Test void rejectsUnsupportedAndUnresolvedDescriptions() throws Exception {
        String spec = spec();
        for (String broken : new String[]{spec.replace("3.0.3", "3.1.0"), spec.replace("#/components/schemas/TenantInput", "https://evil/schema"), spec.replace("additionalProperties: false", "additionalProperties: true"), spec.replace("minLength: 2", "minLength: 2\n          readOnly: true"), spec.replace("    post:", "    delete:")})
            assertThrows(IllegalArgumentException.class, () -> load(broken, Set.of("createTenant")));
        assertThrows(IllegalArgumentException.class, () -> load(spec, Set.of()));
        assertThrows(IllegalArgumentException.class, () -> load(spec, Set.of("missing")));
    }
}
