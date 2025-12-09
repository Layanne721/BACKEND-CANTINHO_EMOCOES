package com.cantinho_emocoes.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(unique = true)
    private String email; 

    @Column
    @JsonIgnore
    private String senha;

    // PIN de 4 dígitos para o Responsável
    @Column
    @JsonIgnore
    private String pin;

    @Column
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Perfil perfil;

    @Column
    private LocalDate dataNascimento; 

    @Column(nullable = false)
    private LocalDate dataCadastro;

    // --- RELACIONAMENTOS ---

    // Filho aponta para o Pai (Responsável)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id")
    @JsonIgnore
    private Usuario responsavel;

    // Pai tem vários Filhos
    @OneToMany(mappedBy = "responsavel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Usuario> dependentes = new ArrayList<>();

    // --- CORREÇÃO DO ERRO 500 (CASCADE) ---
    // Agora, ao apagar o aluno, o banco apaga tudo relacionado a ele automaticamente.

    @OneToMany(mappedBy = "dependente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Diario> diarios = new ArrayList<>();

    @OneToMany(mappedBy = "aluno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Atividade> atividades = new ArrayList<>();

    @OneToMany(mappedBy = "aluno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Avaliacao> avaliacoes = new ArrayList<>();

    // --- CORREÇÃO: Adicionando relacionamento com Tarefa para CASCADE ---
    @OneToMany(mappedBy = "aluno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tarefa> tarefas = new ArrayList<>(); 
    // --------------------------------------------------------------------

    // --- Recuperação de Senha ---
    @Column(name = "reset_token")
    private String resetToken;
    
    @Column(name = "reset_token_expires_at")
    private LocalDateTime resetTokenExpiresAt;
    
    // --- Gamificação ---
    @Column(name = "xp")
    private int xp = 0;

    @Column(name = "nivel")
    private int nivel = 1;

    public Usuario() {}

    // --- Métodos do Spring Security ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.perfil.name()));
    }

    @Override public String getPassword() { return this.senha; }
    @Override public String getUsername() { return this.email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

    // --- Getters e Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public void setSenha(String senha) { this.senha = senha; }
    
    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
    
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    
    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }
    
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    
    public LocalDate getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDate dataCadastro) { this.dataCadastro = dataCadastro; }
    
    public Usuario getResponsavel() { return responsavel; }
    public void setResponsavel(Usuario responsavel) { this.responsavel = responsavel; }
    
    public List<Usuario> getDependentes() { return dependentes; }
    public void setDependentes(List<Usuario> dependentes) { this.dependentes = dependentes; }
    
    public List<Diario> getDiarios() { return diarios; }
    public void setDiarios(List<Diario> diarios) { this.diarios = diarios; }

    public List<Atividade> getAtividades() { return atividades; }
    public void setAtividades(List<Atividade> atividades) { this.atividades = atividades; }

    public List<Avaliacao> getAvaliacoes() { return avaliacoes; }
    public void setAvaliacoes(List<Avaliacao> avaliacoes) { this.avaliacoes = avaliacoes; }

    // --- NOVO GETTER/SETTER PARA TAREFAS ---
    public List<Tarefa> getTarefas() { return tarefas; }
    public void setTarefas(List<Tarefa> tarefas) { this.tarefas = tarefas; }
    // ----------------------------------------
    
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    
    public LocalDateTime getResetTokenExpiresAt() { return resetTokenExpiresAt; }
    public void setResetTokenExpiresAt(LocalDateTime resetTokenExpiresAt) { this.resetTokenExpiresAt = resetTokenExpiresAt; }

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }

    public int getNivel() { return nivel; }
    public void setNivel(int nivel) { this.nivel = nivel; }
}