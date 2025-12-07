package com.cantinho_emocoes.controller;

import com.cantinho_emocoes.dto.SemanarioRequestDTO;
import com.cantinho_emocoes.model.Semanario;
import com.cantinho_emocoes.repository.SemanarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/semanario")
@CrossOrigin(origins = "http://localhost:5173")
public class SemanarioController {

    private final SemanarioRepository semanarioRepository;

    public SemanarioController(SemanarioRepository semanarioRepository) {
        this.semanarioRepository = semanarioRepository;
    }

    // --- SALVAR O SEMANÁRIO (PROFESSOR) ---
    @PostMapping
    public ResponseEntity<?> salvarSemanario(@RequestBody SemanarioRequestDTO dto) {
        try {
            Semanario novoSemanario = new Semanario();
            novoSemanario.setTitulo("Planejamento Semanal");
            
            // Previne erro de nulo se o professor não preencher algum dia
            novoSemanario.setSegunda(dto.segunda() != null ? dto.segunda() : "");
            novoSemanario.setTerca(dto.terca() != null ? dto.terca() : "");
            novoSemanario.setQuarta(dto.quarta() != null ? dto.quarta() : "");
            novoSemanario.setQuinta(dto.quinta() != null ? dto.quinta() : "");
            novoSemanario.setSexta(dto.sexta() != null ? dto.sexta() : "");
            
            novoSemanario.setDataCriacao(LocalDateTime.now());

            semanarioRepository.save(novoSemanario);

            return ResponseEntity.ok(Map.of("message", "Semanário atualizado com sucesso!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Erro ao salvar semanário: " + e.getMessage()));
        }
    }

    // --- BUSCAR O ATUAL (ALUNO E PROFESSOR) ---
    @GetMapping("/atual")
    public ResponseEntity<?> getSemanarioAtual() {
        // Busca o último criado
        Optional<Semanario> ultimo = semanarioRepository.findTopByOrderByDataCriacaoDesc();

        if (ultimo.isEmpty()) {
            // CORREÇÃO DO ERRO 500: Se não existir, retorna um objeto vazio em vez de erro
            Semanario vazio = new Semanario();
            vazio.setSegunda("");
            vazio.setTerca("");
            vazio.setQuarta("");
            vazio.setQuinta("");
            vazio.setSexta("");
            return ResponseEntity.ok(vazio);
        }

        return ResponseEntity.ok(ultimo.get());
    }
}