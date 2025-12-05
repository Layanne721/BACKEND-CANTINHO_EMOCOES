package com.cantinho_emocoes.repository;

import com.cantinho_emocoes.model.Avaliacao;
import com.cantinho_emocoes.model.TipoAvaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    List<Avaliacao> findByAlunoIdOrderByDataAvaliacaoDesc(Long alunoId);
    
    // CORREÇÃO: Retorna List ao invés de Optional para evitar erro de duplicidade
    List<Avaliacao> findByAlunoIdAndTipoAndUnidade(Long alunoId, TipoAvaliacao tipo, String unidade);
}