package com.duoc.biblioteca.bff_app.repository;

import com.duoc.biblioteca.bff_app.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Optional<Usuario> findByRut(String rut);
}