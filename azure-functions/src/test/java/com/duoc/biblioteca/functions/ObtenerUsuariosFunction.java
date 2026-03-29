package com.duoc.biblioteca.functions;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import java.util.Optional;

public class ObtenerUsuariosFunction {

    @FunctionName("ObtenerUsuarios")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", 
                         methods = {HttpMethod.GET}, 
                         authLevel = AuthorizationLevel.ANONYMOUS,
                         route = "usuarios") HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
        context.getLogger().info("Consultando usuarios vía Serverless...");

        // Respuesta simulada de la función (el BFF luego puede cruzar esto con la BD si lo desean)
        String jsonResponse = "[{\"id\": 1, \"rol\": \"Lector\"}, {\"id\": 2, \"rol\": \"Administrador\"}]";

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(jsonResponse)
                .build();
    }
}