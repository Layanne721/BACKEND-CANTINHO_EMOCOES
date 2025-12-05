package com.cantinho_emocoes.repository;

import com.cantinho_emocoes.model.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {
    // Busca a última (mantemos para compatibilidade se precisar)
    Optional<Tarefa> findTopByOrderByDataCriacaoDesc();
    
    // NOVO: Busca as últimas 10 tarefas lançadas pelo professor
    List<Tarefa> findTop10ByOrderByDataCriacaoDesc();
}