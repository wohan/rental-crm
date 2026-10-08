package ru.rentcrm.app.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.security.*;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.*;
import java.util.*;

@Configuration
public class OpenApiConfiguration {
    record Resource(String singular,String plural,String schema,String label,List<String> fields,List<String> output) {}
    private static List<String> fields(String value){return List.of(value.split(","));}
    private static final Map<String,Resource> RESOURCES=Map.ofEntries(
        Map.entry("tenants",new Resource("Tenant","Tenants","Tenant","арендатора",fields("fullName,phone,email,notes"),fields("id,fullName,phone,email"))),
        Map.entry("objects",new Resource("Object","Objects","RentalObject","объект аренды",fields("title,type,address,areaSqm,cadastralNumber,status,monthlyRent,monthlyUtilityAmount,depositAmount,notes"),fields("id,title,address,status,monthlyRent"))),
        Map.entry("contracts",new Resource("Contract","Contracts","LeaseContract","договор",fields("objectId,tenantId,number,startDate,endDate,rentAmount,paymentDay,depositAmount,status,documentUrl,notes"),fields("id,objectId,tenantId,number,startDate,endDate,rentAmount,status"))),
        Map.entry("payments",new Resource("Payment","Payments","Payment","платёж",fields("objectId,tenantId,contractId,dueDate,paidDate,amount,paidAmount,status,type,method,comment"),fields("id,objectId,tenantId,dueDate,amount,paidAmount,status"))),
        Map.entry("expenses",new Resource("Expense","Expenses","Expense","расход",fields("objectId,expenseDate,category,amount,vendor,documentUrl,comment"),fields("id,objectId,expenseDate,category,amount"))),
        Map.entry("maintenance",new Resource("Maintenance","MaintenanceRequests","MaintenanceRequest","заявку на обслуживание",fields("objectId,tenantId,title,description,priority,status,cost,dueDate"),fields("id,objectId,title,priority,status,dueDate,cost"))),
        Map.entry("listings",new Resource("Listing","Listings","Listing","объявление",fields("objectId,title,description,price,publicUrl,published"),fields("id,objectId,title,price,published"))),
        Map.entry("leads",new Resource("Lead","Leads","Lead","лида",fields("listingId,objectId,fullName,phone,email,source,status,comment"),fields("id,fullName,phone,source,status"))),
        Map.entry("documents",new Resource("Document","Documents","DocumentItem","ссылку на документ",fields("objectId,tenantId,contractId,name,type,fileUrl,expiresAt"),fields("id,name,type,fileUrl,expiresAt")))
    );
    @Bean OpenAPI rentalOpenApi(){return new OpenAPI().info(new Info().title("Rental Management API").version("1.0"))
        .components(new Components().addSecuritySchemes("bearerAuth",new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));}
    @Bean OpenApiCustomizer agentOperationPolicy(){return api->{
        // springdoc 2.6 does not propagate every Bean Validation annotation (notably Email/PositiveOrZero).
        // Derive these constraints from the same model annotations used by @Valid.
        for(Resource resource:RESOURCES.values()) {
            Schema<?> schema=api.getComponents().getSchemas().get(resource.schema());
            if(schema==null)continue;
            try {
                for(var field:Class.forName("ru.rentcrm.app.model."+resource.schema()).getFields()) {
                    Schema<?> property=(Schema<?>)schema.getProperties().get(field.getName());
                    if(property==null)continue;
                    if(field.isAnnotationPresent(jakarta.validation.constraints.Email.class))property.setFormat("email");
                    if(field.isAnnotationPresent(jakarta.validation.constraints.NotBlank.class)) {
                        property.setMinLength(Math.max(1,property.getMinLength()==null?0:property.getMinLength()));
                        property.setPattern("\\S");
                    }
                    if(field.isAnnotationPresent(jakarta.validation.constraints.PositiveOrZero.class))property.setMinimum(java.math.BigDecimal.ZERO);
                }
            }catch(ClassNotFoundException e){throw new IllegalStateException(e);}
        }
        api.getPaths().forEach((path,item)->item.readOperationsMap().forEach((method,operation)->{
            operation.addExtension("x-agent-enabled",false);
            operation.addExtension("x-agent-disabled-reason","Technical, sensitive, destructive or unsupported operation; explicit policy review required");
            if(path.startsWith("/api/auth/")||path.startsWith("/api/public/")||path.startsWith("/api/billing/webhooks/"))operation.setSecurity(List.of());
            for(var entry:RESOURCES.entrySet()){
                String base="/api/"+entry.getKey();Resource r=entry.getValue();
                if(path.equals(base)){
                    if(method==PathItem.HttpMethod.GET){
                        operation.setOperationId("list"+r.plural());operation.setSummary("Показать список: "+r.label());enable(operation,r.output());
                    }else if(method==PathItem.HttpMethod.POST){
                        operation.setOperationId("create"+r.singular());operation.setSummary("Создать "+r.label());
                        Schema<?> actual=api.getComponents().getSchemas().get(r.schema());
                        if(actual==null||actual.getProperties()==null)throw new IllegalStateException("Missing schema "+r.schema());
                        ObjectSchema input=new ObjectSchema();input.setAdditionalProperties(false);
                        for(String field:r.fields()){
                            Schema<?> property=(Schema<?>)actual.getProperties().get(field);
                            if(property==null)throw new IllegalStateException("Missing input field "+field);
                            input.addProperty(field,property);
                        }
                        if(actual.getRequired()!=null)input.setRequired(actual.getRequired().stream().filter(r.fields()::contains).toList());
                        String name=r.schema()+"AgentInput";api.getComponents().addSchemas(name,input);
                        operation.addExtension("x-agent-input-schema",Map.of("$ref","#/components/schemas/"+name));enable(operation,r.output());
                    }
                }else if(path.equals(base+"/{id}")){
                    operation.setOperationId((method==PathItem.HttpMethod.DELETE?"delete":"update")+r.singular());
                    operation.setSummary((method==PathItem.HttpMethod.DELETE?"Удалить ":"Заменить ")+r.label());
                    operation.addExtension("x-agent-disabled-reason",method==PathItem.HttpMethod.DELETE?"Destructive operation requires separate policy":"PUT replaces fields: requires current snapshot and concurrency preconditions");
                }
            }
            if(path.equals("/api/payments/{id}/mark-paid")){
                operation.setOperationId("markPaymentPaid");operation.setSummary("Отметить платёж оплаченным");enable(operation,fields("id,status,paidAmount,paidDate"));
            }
        }));
    };}
    private static void enable(Operation operation,List<String> output){operation.addExtension("x-agent-enabled",true);operation.getExtensions().remove("x-agent-disabled-reason");operation.addExtension("x-agent-response-fields",output);}
}
