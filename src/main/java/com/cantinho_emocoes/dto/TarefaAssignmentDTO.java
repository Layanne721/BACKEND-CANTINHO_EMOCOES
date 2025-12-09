package com.cantinho_emocoes.dto;

import java.util.List;

public record TarefaAssignmentDTO(
    String tipo,
    String conteudo,
    List<Long> alunoIds // Lista de IDs dos alunos selecionados
) {}