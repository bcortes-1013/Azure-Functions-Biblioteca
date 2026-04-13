package com.duoc.biblioteca.bff_app.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Entity
@Table(name = "prestamos")
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prestamo")
    @JsonProperty("id_prestamo")
    private Long idPrestamo;

    @Column(name = "id_usuario")
    @JsonProperty("id_usuario")
    private Long idUsuario;

    @Column(name = "isbn_libro")
    @JsonProperty("isbn_libro")
    private String isbnLibro;

    @Column(name = "titulo_libro")
    @JsonProperty("titulo_libro")
    private String tituloLibro;

    @Column(name = "fecha_prestamo", insertable = false)
    @JsonProperty("fecha_prestamo")
    private LocalDate fechaPrestamo;

    @Column(name = "fecha_devolucion")
    @JsonProperty("fecha_devolucion")
    private LocalDate fechaDevolucion;

    private String estado;
}