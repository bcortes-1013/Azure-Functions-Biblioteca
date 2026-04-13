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

    private final RestTemplate restTemplate = new RestTemplate();

    // --- SECCIÓN PRÉSTAMOS (Delegado a Azure FaaS) ---

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


    // --- SECCIÓN USUARIOS (Delegado a Azure FaaS) ---

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

    // --- SECCIÓN PARA GRAPHQL (Nuevos Endpoints) ---

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