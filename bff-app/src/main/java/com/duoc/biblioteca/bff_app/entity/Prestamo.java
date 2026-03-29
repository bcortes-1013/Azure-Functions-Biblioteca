package com.duoc.biblioteca.bff_app.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "prestamos")
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prestamo")
    private Long idPrestamo;

    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "isbn_libro")
    private String isbnLibro;

    @Column(name = "titulo_libro")
    private String tituloLibro;

    @Column(name = "fecha_prestamo", insertable = false)
    private LocalDate fechaPrestamo;

    @Column(name = "fecha_devolucion")
    private LocalDate fechaDevolucion;

    private String estado;
}