package com.cantinho_emocoes.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "semanarios")
public class Semanario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String segunda;

    @Column(columnDefinition = "TEXT")
    private String terca;

    @Column(columnDefinition = "TEXT")
    private String quarta;

    @Column(columnDefinition = "TEXT")
    private String quinta;

    @Column(columnDefinition = "TEXT")
    private String sexta;

    // --- NOVO CAMPO: Armazena a lista de objetivos como JSON (String) ---
    @Column(columnDefinition = "TEXT")
    private String objetivos;

    private LocalDateTime dataCriacao;
    private String titulo; 

    public Semanario() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getSegunda() { return segunda; }
    public void setSegunda(String segunda) { this.segunda = segunda; }

    public String getTerca() { return terca; }
    public void setTerca(String terca) { this.terca = terca; }

    public String getQuarta() { return quarta; }
    public void setQuarta(String quarta) { this.quarta = quarta; }

    public String getQuinta() { return quinta; }
    public void setQuinta(String quinta) { this.quinta = quinta; }

    public String getSexta() { return sexta; }
    public void setSexta(String sexta) { this.sexta = sexta; }

    public String getObjetivos() { return objetivos; }
    public void setObjetivos(String objetivos) { this.objetivos = objetivos; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
}