package com.duoc.biblioteca.functions;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import java.util.Optional;

public class CrearPrestamoFunction {
    
    @FunctionName("CrearPrestamo")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", 
                         methods = {HttpMethod.POST}, 
                         authLevel = AuthorizationLevel.ANONYMOUS,
                         route = "prestamos") HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
        context.getLogger().info("Procesando creación de préstamo vía Serverless...");

        // El cuerpo de la petición viene del BFF
        String body = request.getBody().orElse("");
        
        if (body.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"El cuerpo de la petición no puede estar vacío\"}")
                    .build();
        }

        // Aquí iría la lógica de negocio pura si fuera necesaria. 
        // Como el BFF orquesta, simplemente validamos y respondemos éxito.
        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body("{\"status\": \"CREADO\", \"origen\": \"Azure Function FaaS\", \"datos\": " + body + "}")
                .build();
    }
}