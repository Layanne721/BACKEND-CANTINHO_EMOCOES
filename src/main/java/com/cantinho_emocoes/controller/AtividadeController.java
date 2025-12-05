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
@CrossOrigin(origins = "http://localhost:5173")
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
    public ResponseEntity<?> salvarAtividadeFeita(@RequestHeader("x-child-id") Long childId, @RequestBody Atividade atividade) {
        Usuario aluno = usuarioRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
        
        atividade.setAluno(aluno);
        atividade.setDataRealizacao(LocalDateTime.now());
        atividadeRepository.save(atividade);
        
        return ResponseEntity.ok(Map.of("message", "Atividade entregue com sucesso!"));
    }

    // --- PROFESSOR: LISTAR ATIVIDADES DO ALUNO ---
    @GetMapping("/aluno/{childId}")
    public ResponseEntity<List<Atividade>> listarAtividadesDoAluno(@PathVariable Long childId) {
        return ResponseEntity.ok(atividadeRepository.findByAlunoIdOrderByDataRealizacaoDesc(childId));
    }

    // --- PROFESSOR: ENVIAR TAREFA ---
    @PostMapping("/definir-tarefa")
    public ResponseEntity<?> definirTarefa(@RequestBody Map<String, String> payload) {
        String tipo = payload.get("tipo");
        String conteudo = payload.get("conteudo");

        Tarefa novaTarefa = new Tarefa(tipo, conteudo, LocalDateTime.now());
        tarefaRepository.save(novaTarefa);
        
        long total = tarefaRepository.count();
        return ResponseEntity.ok(Map.of("message", "Tarefa definida e salva!", "totalEnviadas", total));
    }

    // --- ALUNO: TAREFA ATUAL ---
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
        long total = tarefaRepository.count();
        return ResponseEntity.ok(Map.of("total", total));
    }
}