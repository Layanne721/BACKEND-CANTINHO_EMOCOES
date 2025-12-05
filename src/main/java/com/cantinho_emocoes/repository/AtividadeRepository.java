package com.cantinho_emocoes.repository;

import com.cantinho_emocoes.model.Atividade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AtividadeRepository extends JpaRepository<Atividade, Long> {
    List<Atividade> findByAlunoIdOrderByDataRealizacaoDesc(Long alunoId);
}