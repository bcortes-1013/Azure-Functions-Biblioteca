package com.duoc.biblioteca.functions;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import com.google.gson.JsonObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class ConsumidorPrestamos {

    @FunctionName("ConsumidorPrestamos")
    public void run(
            @EventGridTrigger(name = "evento") String contenidoEvento,
            final ExecutionContext context) {

        try {
            // Parseo inteligente a prueba de balas
            com.google.gson.JsonElement raizElement = com.google.gson.JsonParser.parseString(contenidoEvento);

            if (raizElement.isJsonPrimitive() && raizElement.getAsJsonPrimitive().isString()) {
                raizElement = com.google.gson.JsonParser.parseString(raizElement.getAsString());
            }

            if (raizElement.isJsonArray()) {
                raizElement = raizElement.getAsJsonArray().get(0);
            }

            JsonObject eventoJson = raizElement.getAsJsonObject();
            String tipoEvento = eventoJson.get("eventType").getAsString();

            com.google.gson.JsonElement dataElement = eventoJson.get("data");
            if (dataElement.isJsonPrimitive() && dataElement.getAsJsonPrimitive().isString()) {
                dataElement = com.google.gson.JsonParser.parseString(dataElement.getAsString());
            }
            JsonObject data = dataElement.getAsJsonObject();
            
            String dbUrl = System.getenv("SQL_CONNECTION_STRING");
            Class.forName("org.postgresql.Driver");

            try (Connection conn = DriverManager.getConnection(dbUrl)) {
                
                if (tipoEvento.equals("Biblioteca.PrestamoCreado")) {
                    String sql = "INSERT INTO prestamos (id_usuario, isbn_libro, titulo_libro, estado) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, data.get("id_usuario").getAsInt());
                        stmt.setString(2, data.get("isbn_libro").getAsString());
                        stmt.setString(3, data.get("titulo_libro").getAsString());
                        String estado = data.has("estado") ? data.get("estado").getAsString() : "ACTIVO";
                        stmt.setString(4, estado);
                        stmt.executeUpdate();
                        context.getLogger().info("✅ Préstamo INSERTADO correctamente en RDS.");
                    }
                } 
                else if (tipoEvento.equals("Biblioteca.PrestamoActualizado")) {
                    String sql = "UPDATE prestamos SET estado = ? WHERE id_prestamo = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, data.get("estado").getAsString());
                        stmt.setInt(2, data.get("id_prestamo").getAsInt()); // Usamos ID
                        
                        int filasAfectadas = stmt.executeUpdate();
                        if (filasAfectadas == 0) {
                            context.getLogger().warning("⚠️ ALERTA: Se intentó actualizar, pero el id_prestamo no existe en la BD.");
                        } else {
                            context.getLogger().info("🔄 Préstamo ACTUALIZADO en RDS.");
                        }
                    }
                }
                else if (tipoEvento.equals("Biblioteca.PrestamoEliminado")) {
                    String sql = "DELETE FROM prestamos WHERE id_prestamo = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, data.get("id_prestamo").getAsInt()); // Usamos ID
                        
                        int filasAfectadas = stmt.executeUpdate();
                        if (filasAfectadas == 0) {
                            context.getLogger().warning("⚠️ ALERTA: Se intentó eliminar, pero el id_prestamo no fue encontrado.");
                        } else {
                            context.getLogger().info("🗑️ Préstamo ELIMINADO de RDS.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            context.getLogger().severe("❌ Error en Consumidor Prestamos: " + e.getMessage());
        }
    }
}