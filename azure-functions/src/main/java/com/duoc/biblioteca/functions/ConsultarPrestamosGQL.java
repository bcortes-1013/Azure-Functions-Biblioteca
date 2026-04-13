package com.duoc.biblioteca.functions;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.sql.*;
import java.util.Optional;

public class ConsultarPrestamosGQL {
    
    @FunctionName("ConsultarPrestamosGQL")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) 
            HttpRequestMessage<Optional<String>> request, final ExecutionContext context) {
        
        String body = request.getBody().orElse("");
        if (body.isEmpty()) return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Body requerido").build();

        JsonObject jsonRequest = new Gson().fromJson(body, JsonObject.class);
        String query = jsonRequest.get("query").getAsString().toLowerCase();

        try (Connection conn = DriverManager.getConnection(System.getenv("DB_URL"))) {
            Class.forName("org.postgresql.Driver");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM prestamos");
            
            com.google.gson.JsonArray list = new com.google.gson.JsonArray();
            
            while (rs.next()) {
                JsonObject prestamo = new JsonObject();
                
                // Lógica GraphQL: Devolvemos solo lo que pide el query del cliente
                if (query.contains("titulo")) prestamo.addProperty("titulo_libro", rs.getString("titulo_libro"));
                if (query.contains("isbn")) prestamo.addProperty("isbn_libro", rs.getString("isbn_libro"));
                if (query.contains("estado")) prestamo.addProperty("estado", rs.getString("estado"));
                if (query.contains("fecha")) prestamo.addProperty("fecha_prestamo", rs.getString("fecha_prestamo"));
                
                list.add(prestamo);
            }
            
            JsonObject data = new JsonObject();
            data.add("prestamos", list);
            
            return request.createResponseBuilder(HttpStatus.OK).body(data.toString()).build();
            
        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()).build();
        }
    }
}