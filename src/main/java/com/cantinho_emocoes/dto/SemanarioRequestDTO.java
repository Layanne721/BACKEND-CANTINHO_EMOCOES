package com.cantinho_emocoes.dto;

public record SemanarioRequestDTO(
    String segunda,
    String terca,
    String quarta,
    String quinta,
    String sexta,
    String objetivos // Novo campo JSON String
) {}