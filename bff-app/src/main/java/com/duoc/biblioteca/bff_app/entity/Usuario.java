package com.duoc.biblioteca.bff_app.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    @JsonProperty("id_usuario")
    private Long idUsuario;

    private String rut;
    private String nombre;
    private String email;

    // insertable = false porque Oracle le pone SYSDATE por defecto según nuestro init.sql
    @Column(name = "fecha_registro", insertable = false)
    @JsonProperty("fecha_registro")
    private LocalDate fechaRegistro; 
}