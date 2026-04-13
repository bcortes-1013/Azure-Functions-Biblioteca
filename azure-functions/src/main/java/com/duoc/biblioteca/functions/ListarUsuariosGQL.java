package com.duoc.biblioteca.functions;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.sql.*;
import java.util.Optional;

public class ListarUsuariosGQL {
    
    @FunctionName("ListarUsuariosGQL")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) 
            HttpRequestMessage<Optional<String>> request, final ExecutionContext context) {
        
        String body = request.getBody().orElse("");
        
        // 1. Validación inicial
        if (body.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("{\"error\": \"Body con query es requerido\"}").build();
        }

        // 2. Extraer el query simulado de GraphQL
        JsonObject jsonRequest = new Gson().fromJson(body, JsonObject.class);
        String query = jsonRequest.get("query").getAsString().toLowerCase();

        // 3. Conexión a la Base de Datos (AWS RDS)
        try (Connection conn = DriverManager.getConnection(System.getenv("DB_URL"))) {
            // Aseguramos cargar el driver de PostgreSQL
            Class.forName("org.postgresql.Driver");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM usuarios");
            
            com.google.gson.JsonArray list = new com.google.gson.JsonArray();
            
            // 4. Procesar resultados aplicando el filtro GraphQL
            while (rs.next()) {
                JsonObject usuario = new JsonObject();
                
                // Lógica GraphQL: Devolvemos SOLO lo que pide el query del cliente
                if (query.contains("id") || query.contains("id_usuario")) {
                    usuario.addProperty("id_usuario", rs.getLong("id_usuario"));
                }
                if (query.contains("rut")) {
                    usuario.addProperty("rut", rs.getString("rut"));
                }
                if (query.contains("nombre")) {
                    usuario.addProperty("nombre", rs.getString("nombre"));
                }
                if (query.contains("email")) {
                    usuario.addProperty("email", rs.getString("email"));
                }
                
                list.add(usuario);
            }
            
            // 5. Empaquetar en el formato estándar de GraphQL {"data": { ... }}
            JsonObject data = new JsonObject();
            data.add("usuarios", list);
            
            return request.createResponseBuilder(HttpStatus.OK).body(data.toString()).build();
            
        } catch (Exception e) {
            context.getLogger().severe("Error en GraphQL Usuarios: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                          .body("{\"error\": \"" + e.getMessage() + "\"}")
                          .build();
        }
    }
}