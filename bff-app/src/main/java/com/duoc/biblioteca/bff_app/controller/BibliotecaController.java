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
}