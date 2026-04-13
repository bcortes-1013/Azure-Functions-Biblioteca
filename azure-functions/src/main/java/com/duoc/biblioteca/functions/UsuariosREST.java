package com.duoc.biblioteca.functions;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.sql.*;
import java.util.Optional;

public class UsuariosREST {
    @FunctionName("UsuariosREST")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE}, 
            authLevel = AuthorizationLevel.ANONYMOUS, route = "usuarios/{id?}") 
            HttpRequestMessage<Optional<String>> request, final ExecutionContext context) {
        
        String dbUrl = System.getenv("DB_URL");
        String method = request.getHttpMethod().name();
        String body = request.getBody().orElse("{}");
        Gson gson = new Gson();

        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            Class.forName("org.postgresql.Driver");
            
            // --- MÉTODO GET: Listar todos los usuarios ---
            if (method.equals("GET")) {
                com.google.gson.JsonArray list = new com.google.gson.JsonArray();
                try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM usuarios");
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        JsonObject u = new JsonObject();
                        u.addProperty("id_usuario", rs.getLong("id_usuario"));
                        u.addProperty("rut", rs.getString("rut")); // Corregido a String
                        u.addProperty("nombre", rs.getString("nombre"));
                        u.addProperty("email", rs.getString("email"));
                        list.add(u);
                    }
                }
                return request.createResponseBuilder(HttpStatus.OK).body(list.toString()).build();

            // --- MÉTODO POST: Crear Usuario ---
            } else if (method.equals("POST")) {
                JsonObject json = gson.fromJson(body, JsonObject.class);
                try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO usuarios (rut, nombre, email) VALUES (?, ?, ?)")) {
                    stmt.setString(1, json.get("rut").getAsString()); // Corregido a String
                    stmt.setString(2, json.get("nombre").getAsString());
                    stmt.setString(3, json.get("email").getAsString());
                    stmt.executeUpdate();
                }
                return request.createResponseBuilder(HttpStatus.CREATED).body("{\"status\": \"Usuario Creado\"}").build();

            // --- MÉTODO PUT: Actualizar Usuario ---
            } else if (method.equals("PUT")) {
                JsonObject json = gson.fromJson(body, JsonObject.class);
                try (PreparedStatement stmt = conn.prepareStatement("UPDATE usuarios SET rut = ?, nombre = ?, email = ? WHERE id_usuario = ?")) { 
                    stmt.setString(1, json.get("rut").getAsString()); // Corregido a String
                    stmt.setString(2, json.get("nombre").getAsString());
                    stmt.setString(3, json.get("email").getAsString());
                    stmt.setLong(4, json.get("id_usuario").getAsLong());
                    stmt.executeUpdate();
                }
                return request.createResponseBuilder(HttpStatus.OK).body("{\"status\": \"Usuario Actualizado\"}").build();

            // --- MÉTODO DELETE: Eliminar Usuario ---
            } else if (method.equals("DELETE")) {
                // Borrado por ID en la ruta (compatible con el BFF)
                String idStr = request.getUri().getPath().substring(request.getUri().getPath().lastIndexOf('/') + 1);
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM usuarios WHERE id_usuario = ?")) { 
                    stmt.setLong(1, Long.parseLong(idStr));
                    stmt.executeUpdate();
                }
                return request.createResponseBuilder(HttpStatus.OK).body("{\"status\": \"Usuario Eliminado\"}").build();
            }
        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
        return request.createResponseBuilder(HttpStatus.BAD_REQUEST).build();
    }
}