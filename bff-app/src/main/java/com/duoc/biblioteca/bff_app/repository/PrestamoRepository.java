package com.duoc.biblioteca.bff_app.repository;

import com.duoc.biblioteca.bff_app.entity.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
}