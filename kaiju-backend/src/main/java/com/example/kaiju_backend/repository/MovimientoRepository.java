package com.example.kaiju_backend.repository;

import com.example.kaiju_backend.model.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    List<Movimiento> findByProductoIdOrderByIdDesc(Long productoId);
}