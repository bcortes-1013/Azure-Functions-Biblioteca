package com.duoc.biblioteca.functions;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.sql.*;
import java.util.Optional;

public class PrestamosREST {
    @FunctionName("PrestamosREST")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE}, 
            authLevel = AuthorizationLevel.ANONYMOUS, route = "prestamos/{id?}") 
            HttpRequestMessage<Optional<String>> request, final ExecutionContext context) {
        
        String dbUrl = System.getenv("DB_URL");
        String method = request.getHttpMethod().name();
        String body = request.getBody().orElse("{}");
        Gson gson = new Gson();

        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            Class.forName("org.postgresql.Driver");
            
            // --- MÉTODO GET: Listar todos los préstamos ---
            if (method.equals("GET")) {
                com.google.gson.JsonArray list = new com.google.gson.JsonArray();
                try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM prestamos");
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        JsonObject p = new JsonObject();
                        p.addProperty("id_prestamo", rs.getLong("id_prestamo"));
                        p.addProperty("id_usuario", rs.getLong("id_usuario"));
                        p.addProperty("titulo_libro", rs.getString("titulo_libro"));
                        p.addProperty("isbn_libro", rs.getString("isbn_libro"));
                        p.addProperty("estado", rs.getString("estado"));
                        list.add(p);
                    }
                }
                return request.createResponseBuilder(HttpStatus.OK).body(list.toString()).build();

            // --- MÉTODO POST: Crear Préstamo ---
            } else if (method.equals("POST")) {
                JsonObject json = gson.fromJson(body, JsonObject.class);
                try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO prestamos (id_usuario, isbn_libro, titulo_libro, estado) VALUES (?, ?, ?, ?)")) {
                    stmt.setLong(1, json.get("id_usuario").getAsLong());
                    stmt.setString(2, json.get("isbn_libro").getAsString());
                    stmt.setString(3, json.get("titulo_libro").getAsString());
                    stmt.setString(4, json.get("estado").getAsString());
                    stmt.executeUpdate();
                }
                return request.createResponseBuilder(HttpStatus.CREATED).body("{\"status\": \"Préstamo Creado\"}").build();

            // --- MÉTODO PUT: Actualizar Préstamo ---
            } else if (method.equals("PUT")) {
                JsonObject json = gson.fromJson(body, JsonObject.class);
                try (PreparedStatement stmt = conn.prepareStatement("UPDATE prestamos SET estado = ?, titulo_libro = ? WHERE id_prestamo = ?")) { 
                    stmt.setString(1, json.get("estado").getAsString());
                    stmt.setString(2, json.get("titulo_libro").getAsString());
                    stmt.setLong(3, json.get("id_prestamo").getAsLong());
                    stmt.executeUpdate();
                }
                return request.createResponseBuilder(HttpStatus.OK).body("{\"status\": \"Préstamo Actualizado\"}").build();

            // --- MÉTODO DELETE: Eliminar Préstamo ---
            } else if (method.equals("DELETE")) {
                // Borrado por ID en la ruta (compatible con el BFF)
                String idStr = request.getUri().getPath().substring(request.getUri().getPath().lastIndexOf('/') + 1);
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM prestamos WHERE id_prestamo = ?")) { 
                    stmt.setLong(1, Long.parseLong(idStr));
                    stmt.executeUpdate();
                }
                return request.createResponseBuilder(HttpStatus.OK).body("{\"status\": \"Préstamo Eliminado\"}").build();
            }
        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
        return request.createResponseBuilder(HttpStatus.BAD_REQUEST).build();
    }
}