package com.duoc.biblioteca.bff_app.controller;

import com.duoc.biblioteca.bff_app.entity.Prestamo;
import com.duoc.biblioteca.bff_app.repository.PrestamoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/api/biblioteca")
public class BibliotecaController {

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Value("${azure.functions.url}")
    private String azureFunctionsUrl;

    // Instancia para hacer peticiones HTTP a las Azure Functions
    private final RestTemplate restTemplate = new RestTemplate();

    // Endpoint 1: Obtener todos los préstamos (Consulta local a Oracle)
    @GetMapping("/prestamos")
    public ResponseEntity<List<Prestamo>> obtenerPrestamos() {
        return ResponseEntity.ok(prestamoRepository.findAll());
    }

    // Endpoint 2: Crear un préstamo (Orquesta: Guarda en DB y llama a la Function)
    @PostMapping("/prestamos")
    public ResponseEntity<String> procesarPrestamoServerless(@RequestBody Prestamo prestamo) {
        // 1. Guardar en Oracle localmente
        prestamoRepository.save(prestamo);

        // 2. Orquestar la llamada a la Azure Function (FaaS)
        String functionEndpoint = azureFunctionsUrl + "CrearPrestamoFunction";
        
        try {
            // El BFF envía los datos a la función serverless
            ResponseEntity<String> response = restTemplate.postForEntity(functionEndpoint, prestamo, String.class);
            
            // 3. Devolver la respuesta en formato JSON al cliente
            return ResponseEntity.ok("{\"mensaje\": \"Préstamo procesado correctamente mediante Serverless\", \"detalle\": " + response.getBody() + "}");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("{\"error\": \"Fallo al comunicar con Azure Functions\"}");
        }
    }
}