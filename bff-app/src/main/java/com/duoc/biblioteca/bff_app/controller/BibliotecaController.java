package com.duoc.biblioteca.bff_app.controller;

import com.duoc.biblioteca.bff_app.entity.Prestamo;
import com.duoc.biblioteca.bff_app.entity.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/biblioteca")
public class BibliotecaController {

    @Value("${azure.functions.url}")
    private String azureFunctionsUrl;

    @Value("${azure.productor.usuarios.url}")
    private String urlProductorUsuarios;

    @Value("${azure.productor.prestamos.url}")
    private String urlProductorPrestamos;

    private final RestTemplate restTemplate = new RestTemplate();

    // ====================================================================
    // 1. SECCIÓN SINCRÓNICA ORIGINAL (CRUD Directo FaaS)
    // ====================================================================

    // PRÉSTAMOS

    @GetMapping("/prestamos")
    public ResponseEntity<String> obtenerPrestamos() {
        String url = azureFunctionsUrl + "prestamos";
        return ResponseEntity.ok(restTemplate.getForObject(url, String.class));
    }

    @PostMapping("/prestamos")
    public ResponseEntity<String> procesarPrestamoServerless(@RequestBody Prestamo prestamo) {
        String url = azureFunctionsUrl + "prestamos";
        return restTemplate.postForEntity(url, prestamo, String.class);
    }

    @PutMapping("/prestamos")
    public ResponseEntity<String> actualizarPrestamo(@RequestBody Prestamo prestamo) {
        String url = azureFunctionsUrl + "prestamos";
        restTemplate.put(url, prestamo);
        return ResponseEntity.ok("{\"status\": \"Solicitud de actualización enviada\"}");
    }

    @DeleteMapping("/prestamos/{id}")
    public ResponseEntity<String> eliminarPrestamo(@PathVariable Long id) {
        String url = azureFunctionsUrl + "prestamos/" + id;
        restTemplate.delete(url);
        return ResponseEntity.ok("{\"status\": \"Solicitud de eliminación enviada\"}");
    }

    // USUARIOS

    @GetMapping("/usuarios")
    public ResponseEntity<String> obtenerUsuarios() {
        String url = azureFunctionsUrl + "usuarios";
        return ResponseEntity.ok(restTemplate.getForObject(url, String.class));
    }

    @PostMapping("/usuarios")
    public ResponseEntity<String> guardarUsuario(@RequestBody Usuario usuario) {
        String url = azureFunctionsUrl + "usuarios";
        return restTemplate.postForEntity(url, usuario, String.class);
    }

    @PutMapping("/usuarios")
    public ResponseEntity<String> actualizarUsuario(@RequestBody Usuario usuario) {
        String url = azureFunctionsUrl + "usuarios";
        restTemplate.put(url, usuario);
        return ResponseEntity.ok("{\"status\": \"Solicitud de actualización enviada\"}");
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<String> eliminarUsuario(@PathVariable Long id) {
        String url = azureFunctionsUrl + "usuarios/" + id;
        restTemplate.delete(url);
        return ResponseEntity.ok("{\"status\": \"Solicitud de eliminación enviada\"}");
    }

    // ====================================================================
    // 2. SECCIÓN ASINCRÓNICA (Nuevos Endpoints para Event Grid)
    // ====================================================================

    // PRÉSTAMOS

    @PostMapping("/eventos/prestamos")
    public ResponseEntity<String> eventoCrearPrestamo(@RequestBody Prestamo prestamo) {
        return restTemplate.postForEntity(urlProductorPrestamos, prestamo, String.class);
    }

    @PutMapping("/eventos/prestamos")
    public ResponseEntity<String> eventoActualizarPrestamo(@RequestBody Prestamo prestamo) {
         restTemplate.put(urlProductorPrestamos, prestamo);
         return ResponseEntity.ok("{\"status\": \"Evento PUT de préstamo enviado a Azure Event Grid\"}");
    }

    @DeleteMapping("/eventos/prestamos/{id}")
    public ResponseEntity<String> eventoEliminarPrestamo(@PathVariable Long id) {
         restTemplate.delete(urlProductorPrestamos + "?id=" + id);
         return ResponseEntity.ok("{\"status\": \"Evento DELETE de préstamo enviado a Azure Event Grid\"}");
    }

    // USUARIOS

    @PostMapping("/eventos/usuarios")
    public ResponseEntity<String> eventoCrearUsuario(@RequestBody Usuario usuario) {
        return restTemplate.postForEntity(urlProductorUsuarios, usuario, String.class);
    }

    @PutMapping("/eventos/usuarios")
    public ResponseEntity<String> eventoActualizarUsuario(@RequestBody Usuario usuario) {
         restTemplate.put(urlProductorUsuarios, usuario);
         return ResponseEntity.ok("{\"status\": \"Evento PUT de usuario enviado a Azure Event Grid\"}");
    }

    @DeleteMapping("/eventos/usuarios/{id}")
    public ResponseEntity<String> eventoEliminarUsuario(@PathVariable Long id) {
         restTemplate.delete(urlProductorUsuarios + "?id=" + id);
         return ResponseEntity.ok("{\"status\": \"Evento DELETE de usuario enviado a Azure Event Grid\"}");
    }

    // ====================================================================
    // 3. SECCIÓN PARA GRAPHQL
    // ====================================================================

    @PostMapping("/graphql/prestamos")
    public ResponseEntity<String> consultaGraphQLPrestamos(@RequestBody String query) {
        String url = azureFunctionsUrl + "ConsultarPrestamosGQL";
        return restTemplate.postForEntity(url, query, String.class);
    }

    @PostMapping("/graphql/usuarios")
    public ResponseEntity<String> consultaGraphQLUsuarios(@RequestBody String query) {
        String url = azureFunctionsUrl + "ListarUsuariosGQL";
        return restTemplate.postForEntity(url, query, String.class);
    }
}