package com.cantinho_emocoes.controller;

import com.cantinho_emocoes.dto.SemanarioRequestDTO;
import com.cantinho_emocoes.model.Semanario;
import com.cantinho_emocoes.repository.SemanarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/semanario")
@CrossOrigin(origins = "http://localhost:5173")
public class SemanarioController {

    private final SemanarioRepository semanarioRepository;

    public SemanarioController(SemanarioRepository semanarioRepository) {
        this.semanarioRepository = semanarioRepository;
    }

    // --- PROFESSOR: CRIAR/ATUALIZAR SEMANÁRIO ---
    @PostMapping("/definir")
    public ResponseEntity<?> definirSemanario(@RequestBody SemanarioRequestDTO dto) {
        // Cria um novo registro para manter um histórico dos planos semanais
        Semanario novoSemanario = new Semanario();
        novoSemanario.setTextoSemanario(dto.texto());
        
        LocalDateTime now = LocalDateTime.now();
        // Gera um título mais amigável com a data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        novoSemanario.setTitulo("Semana de " + now.toLocalDate().format(formatter));
        
        novoSemanario.setDataCriacao(now);
        
        semanarioRepository.save(novoSemanario);
        
        return ResponseEntity.ok(Map.of("message", "Semanário definido e enviado para os alunos!"));
    }

    // --- ALUNO/PROFESSOR: BUSCAR SEMANÁRIO ATUAL ---
    @GetMapping("/atual")
    public ResponseEntity<?> getSemanarioAtual() {
        Optional<Semanario> ultimo = semanarioRepository.findTopByOrderByDataCriacaoDesc();
        
        if (ultimo.isEmpty()) {
            return ResponseEntity.ok(Map.of("textoSemanario", "Nenhum semanário definido pelo professor.", "titulo", "Aguardando..."));
        }
        
        Semanario s = ultimo.get();
        return ResponseEntity.ok(Map.of("textoSemanario", s.getTextoSemanario(), "titulo", s.getTitulo()));
    }
}