package com.cantinho_emocoes.repository;

import com.cantinho_emocoes.model.Atividade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AtividadeRepository extends JpaRepository<Atividade, Long> {
    // CORREÇÃO: Adicionado o "_" (underscore) para garantir que o JPA entenda a relação com Aluno
    List<Atividade> findByAluno_IdOrderByDataRealizacaoDesc(Long alunoId);
    
    // Mantemos este método chamando o correto para compatibilidade, caso seja usado em outro lugar
    default List<Atividade> findByAlunoIdOrderByDataRealizacaoDesc(Long alunoId) {
        return findByAluno_IdOrderByDataRealizacaoDesc(alunoId);
    }
}