package com.cantinho_emocoes.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "semanarios")
public class Semanario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Campo para armazenar o texto completo do Semanário (estrutura semanal)
    @Column(columnDefinition = "TEXT", nullable = false)
    private String textoSemanario; 

    private LocalDateTime dataCriacao;
    
    // Campo para identificar a semana
    private String titulo; 

    public Semanario() {}

    public Semanario(String textoSemanario, String titulo, LocalDateTime dataCriacao) {
        this.textoSemanario = textoSemanario;
        this.titulo = titulo;
        this.dataCriacao = dataCriacao;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTextoSemanario() { return textoSemanario; }
    public void setTextoSemanario(String textoSemanario) { this.textoSemanario = textoSemanario; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
}