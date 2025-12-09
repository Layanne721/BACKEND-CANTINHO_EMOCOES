package com.cantinho_emocoes.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tarefas")
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipo; // VOGAL, NUMERO, ALFABETO, LIVRE
    private String conteudo; // A, B, 1...

    private LocalDateTime dataCriacao;
    
    // --- NOVO CAMPO: Aluno para quem a tarefa foi atribuída ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id") 
    @JsonIgnore
    private Usuario aluno; 

    public Tarefa() {}

    public Tarefa(String tipo, String conteudo, LocalDateTime dataCriacao) {
        this.tipo = tipo;
        this.conteudo = conteudo;
        this.dataCriacao = dataCriacao;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    
    // NOVO GETTER/SETTER
    public Usuario getAluno() { return aluno; }
    public void setAluno(Usuario aluno) { this.aluno = aluno; }
}