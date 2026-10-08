package ru.rentcrm.app;

import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:openapi;MODE=PostgreSQL;DB_CLOSE_DELAY=-1","spring.datasource.driver-class-name=org.h2.Driver",
 "spring.jpa.hibernate.ddl-auto=create-drop","spring.flyway.enabled=false","text-agent.enabled=false",
 "APP_FIELD_ENCRYPTION_KEY=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","app.field-encryption-key=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="})
@AutoConfigureMockMvc
class OpenApiContractTest {
    @Autowired MockMvc mvc;@Autowired ObjectMapper json;@Autowired @org.springframework.beans.factory.annotation.Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping mappings;
    @Test void exportAndVerifyAllControllerOperations()throws Exception{
        mvc.perform(get("/api/openapi")).andExpect(status().isForbidden());
        String text=mvc.perform(get("/api/openapi").with(user("schema-test"))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        JsonNode spec=json.readTree(text);Set<String> ids=new HashSet<>();int total=0,enabled=0;
        for(var it=spec.path("paths").fields();it.hasNext();){var p=it.next();for(var methods=p.getValue().fields();methods.hasNext();){var m=methods.next();if(!Set.of("get","post","put","delete","patch").contains(m.getKey()))continue;
            total++;assertTrue(ids.add(m.getValue().path("operationId").asText()));if(m.getValue().path("x-agent-enabled").asBoolean())enabled++;
        }}
        for(var entry:mappings.getHandlerMethods().entrySet()){
            if(!entry.getValue().getBeanType().getPackageName().equals("ru.rentcrm.app.controller"))continue;
            for(String path:entry.getKey().getPatternValues())for(var method:entry.getKey().getMethodsCondition().getMethods())
                assertTrue(spec.path("paths").path(path).has(method.name().toLowerCase(Locale.ROOT)),"Missing "+method+" "+path);
        }
        assertTrue(total>=50);assertEquals(19,enabled);
        JsonNode contract=spec.path("components").path("schemas").path("LeaseContractAgentInput");
        assertTrue(contract.path("required").toString().contains("objectId"));assertEquals(31,contract.path("properties").path("paymentDay").path("maximum").asInt());
        assertFalse(spec.path("paths").path("/api/account/change-password").path("post").path("x-agent-enabled").asBoolean());
        Path output=Path.of("target/rental-openapi.json");Files.createDirectories(output.getParent());Files.writeString(output,json.writerWithDefaultPrettyPrinter().writeValueAsString(spec));
        if(Boolean.getBoolean("agent.openapi.export"))Files.copy(output,Path.of("src/main/resources/rental-openapi.json"),StandardCopyOption.REPLACE_EXISTING);
        else {
            JsonNode snapshot=json.readTree(getClass().getResourceAsStream("/rental-openapi.json"));
            assertEquals(snapshot.path("paths"),spec.path("paths"),"Regenerate OpenAPI snapshot with -Dagent.openapi.export=true");
            assertEquals(snapshot.path("components"),spec.path("components"),"OpenAPI schema drift");
        }
    }
}
