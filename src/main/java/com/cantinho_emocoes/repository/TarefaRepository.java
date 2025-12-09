package com.cantinho_emocoes.repository;

import com.cantinho_emocoes.model.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {
    // Busca a última (mantemos para compatibilidade se precisar)
    Optional<Tarefa> findTopByOrderByDataCriacaoDesc();
    
    // Busca as últimas 10 tarefas lançadas (agora são atribuições)
    List<Tarefa> findTop10ByOrderByDataCriacaoDesc();
    
    // NOVO: Busca todas as tarefas atribuídas a um aluno (para rastreamento de pendentes)
    List<Tarefa> findByAlunoIdOrderByDataCriacaoDesc(Long alunoId);
    
    // NOVO: Conta o total de tarefas atribuídas a um aluno (para o dashboard)
    long countByAlunoId(Long alunoId);
}