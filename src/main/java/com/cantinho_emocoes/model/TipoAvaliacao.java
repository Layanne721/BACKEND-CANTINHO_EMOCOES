package com.cantinho_emocoes.model;

public enum TipoAvaliacao {
    EF("Escuta, Fala, Pensamento e Imaginação"),
    EO("Eu, o Outro, o Nós"),
    TS("Traços, Sons, Cores e Formas"),
    ET("Espaços, Tempos, Quantidades, Relações e Transformações");

    private final String descricao;

    TipoAvaliacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}