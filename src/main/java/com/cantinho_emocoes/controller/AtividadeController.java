package com.cantinho_emocoes.controller;

import com.cantinho_emocoes.model.Atividade;
import com.cantinho_emocoes.model.Tarefa;
import com.cantinho_emocoes.model.Usuario;
import com.cantinho_emocoes.repository.AtividadeRepository;
import com.cantinho_emocoes.repository.TarefaRepository;
import com.cantinho_emocoes.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/atividades")

public class AtividadeController {

    private final AtividadeRepository atividadeRepository;
    private final UsuarioRepository usuarioRepository;
    private final TarefaRepository tarefaRepository;

    public AtividadeController(AtividadeRepository atividadeRepository, 
                               UsuarioRepository usuarioRepository,
                               TarefaRepository tarefaRepository) {
        this.atividadeRepository = atividadeRepository;
        this.usuarioRepository = usuarioRepository;
        this.tarefaRepository = tarefaRepository;
    }

    // --- ALUNO: SALVAR ATIVIDADE ---
    @PostMapping
    public ResponseEntity<?> salvarAtividadeFeita(@RequestHeader("x-child-id") Long childId, @RequestBody Map<String, String> payload) {
        Usuario aluno = usuarioRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));

        Atividade nova = new Atividade();
        nova.setTipo(payload.get("tipo"));
        nova.setConteudo(payload.get("conteudo"));
        nova.setDesenhoBase64(payload.get("desenhoBase64"));
        nova.setDataRealizacao(LocalDateTime.now());
        nova.setAluno(aluno);

        atividadeRepository.save(nova);
        return ResponseEntity.ok(Map.of("message", "Atividade salva!"));
    }

    // --- ALUNO: LISTAR MINHAS ATIVIDADES ---
    @GetMapping("/aluno/{childId}")
    public ResponseEntity<List<Atividade>> listarAtividadesAluno(@PathVariable Long childId) {
        return ResponseEntity.ok(atividadeRepository.findByAlunoIdOrderByDataRealizacaoDesc(childId));
    }

    // --- PROFESSOR: DEFINIR TAREFA DA TURMA ---
    @PostMapping("/definir-tarefa")
    public ResponseEntity<?> definirTarefa(@RequestBody Map<String, String> payload) {
        Tarefa t = new Tarefa();
        t.setTipo(payload.get("tipo"));
        t.setConteudo(payload.get("conteudo"));
        t.setDataCriacao(LocalDateTime.now());
        tarefaRepository.save(t);
        return ResponseEntity.ok(Map.of("message", "Tarefa definida para todos!"));
    }

    // --- ALUNO: VER TAREFA ATUAL ---
    @GetMapping("/tarefa-atual")
    public ResponseEntity<?> getTarefaAtual() {
        Optional<Tarefa> ultima = tarefaRepository.findTopByOrderByDataCriacaoDesc();
        if (ultima.isEmpty()) {
            return ResponseEntity.ok(Map.of("tipo", "LIVRE", "conteudo", ""));
        }
        return ResponseEntity.ok(ultima.get());
    }

    // --- ALUNO: PENDÊNCIAS ---
    @GetMapping("/pendentes")
    public ResponseEntity<?> getTarefasPendentes(@RequestHeader("x-child-id") Long childId) {
        List<Tarefa> ultimasTarefas = tarefaRepository.findTop10ByOrderByDataCriacaoDesc();
        List<Atividade> atividadesFeitas = atividadeRepository.findByAlunoIdOrderByDataRealizacaoDesc(childId);

        List<Tarefa> pendentes = ultimasTarefas.stream()
            .filter(tarefa -> {
                if ("LIVRE".equalsIgnoreCase(tarefa.getTipo())) return false;
                boolean jaFez = atividadesFeitas.stream().anyMatch(a -> 
                    a.getTipo().equals(tarefa.getTipo()) && 
                    a.getConteudo().equals(tarefa.getConteudo())
                );
                return !jaFez;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(pendentes);
    }
    
    // --- TOTAL ENVIADAS ---
    @GetMapping("/total-enviadas")
    public ResponseEntity<?> getTotalEnviadas() {
        long count = tarefaRepository.count(); 
        return ResponseEntity.ok(Map.of("total", count));
    }
}