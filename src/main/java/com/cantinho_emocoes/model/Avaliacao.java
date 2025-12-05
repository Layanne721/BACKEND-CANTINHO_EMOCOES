package com.cantinho_emocoes.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "avaliacoes")
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoAvaliacao tipo; // EF, EO, TS, ET

    private String unidade; // Ex: "III UNIDADE"

    private LocalDateTime dataAvaliacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    @JsonIgnore
    private Usuario aluno;

    // Armazena as respostas como JSON ou Map simples (Ex: "1": "S", "2": "N")
    // Para simplificar no Postgres sem configurar conversores complexos, vamos usar ElementCollection
    @ElementCollection
    @CollectionTable(name = "avaliacao_respostas", joinColumns = @JoinColumn(name = "avaliacao_id"))
    @MapKeyColumn(name = "numero_questao")
    @Column(name = "resposta") // S, N, NA
    private Map<String, String> respostas;

    private String observacao;

    public Avaliacao() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TipoAvaliacao getTipo() { return tipo; }
    public void setTipo(TipoAvaliacao tipo) { this.tipo = tipo; }
    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }
    public LocalDateTime getDataAvaliacao() { return dataAvaliacao; }
    public void setDataAvaliacao(LocalDateTime dataAvaliacao) { this.dataAvaliacao = dataAvaliacao; }
    public Usuario getAluno() { return aluno; }
    public void setAluno(Usuario aluno) { this.aluno = aluno; }
    public Map<String, String> getRespostas() { return respostas; }
    public void setRespostas(Map<String, String> respostas) { this.respostas = respostas; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}