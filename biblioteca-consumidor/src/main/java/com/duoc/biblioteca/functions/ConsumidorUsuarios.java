package com.duoc.biblioteca.functions;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import com.google.gson.JsonObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class ConsumidorUsuarios {

    @FunctionName("ConsumidorUsuarios")
    public void run(
            @EventGridTrigger(name = "evento") String contenidoEvento,
            final ExecutionContext context) {

        try {
            // Parseo inteligente
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
                
                if (tipoEvento.equals("Biblioteca.UsuarioCreado")) {
                    String sql = "INSERT INTO usuarios (rut, nombre, email) VALUES (?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, data.get("rut").getAsString());
                        stmt.setString(2, data.get("nombre").getAsString());
                        stmt.setString(3, data.get("email").getAsString());
                        stmt.executeUpdate();
                        context.getLogger().info("✅ Usuario INSERTADO correctamente en RDS.");
                    }
                }
                else if (tipoEvento.equals("Biblioteca.UsuarioActualizado")) {
                    // Actualizamos buscando por el ID numérico
                    String sql = "UPDATE usuarios SET nombre = ?, email = ?, rut = ? WHERE id_usuario = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, data.get("nombre").getAsString());
                        stmt.setString(2, data.get("email").getAsString());
                        stmt.setString(3, data.get("rut").getAsString());
                        stmt.setInt(4, data.get("id_usuario").getAsInt());
                        
                        int filasAfectadas = stmt.executeUpdate();
                        if (filasAfectadas == 0) {
                            context.getLogger().warning("⚠️ ALERTA: Se intentó actualizar, pero el id_usuario no existe en la BD.");
                        } else {
                            context.getLogger().info("🔄 Usuario ACTUALIZADO en RDS.");
                        }
                    }
                }
                else if (tipoEvento.equals("Biblioteca.UsuarioEliminado")) {
                    // Eliminamos buscando por el ID numérico
                    String sql = "DELETE FROM usuarios WHERE id_usuario = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, data.get("id_usuario").getAsInt());
                        
                        int filasAfectadas = stmt.executeUpdate();
                        if (filasAfectadas == 0) {
                            context.getLogger().warning("⚠️ ALERTA: Se intentó eliminar, pero el id_usuario no fue encontrado.");
                        } else {
                            context.getLogger().info("🗑️ Usuario ELIMINADO de RDS.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            context.getLogger().severe("❌ Error en Consumidor Usuarios: " + e.getMessage());
        }
    }
}