package com.cantinho_emocoes.repository;

import com.cantinho_emocoes.model.Semanario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SemanarioRepository extends JpaRepository<Semanario, Long> {
    // Buscar o último Semanário criado
    Optional<Semanario> findTopByOrderByDataCriacaoDesc();
}