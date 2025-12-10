package com.cantinho_emocoes.controller;

import com.cantinho_emocoes.model.Atividade;
import com.cantinho_emocoes.model.Tarefa;
import com.cantinho_emocoes.model.Usuario;
import com.cantinho_emocoes.repository.AtividadeRepository;
import com.cantinho_emocoes.repository.TarefaRepository;
import com.cantinho_emocoes.repository.UsuarioRepository;
import com.cantinho_emocoes.dto.TarefaAssignmentDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    // --- SALVAR ATIVIDADE (ALUNO) ---
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

    @GetMapping("/aluno/{childId}")
    public ResponseEntity<List<Atividade>> listarAtividadesAluno(@PathVariable Long childId) {
        return ResponseEntity.ok(atividadeRepository.findByAlunoIdOrderByDataRealizacaoDesc(childId));
    }

    // --- EXCLUIR ATIVIDADE (PROFESSOR) ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirAtividadeRealizada(@PathVariable Long id) {
        if (atividadeRepository.existsById(id)) {
            atividadeRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // --- TAREFAS (ASSIGNMENTS) ---

    @PostMapping("/definir-tarefa")
    public ResponseEntity<?> definirTarefa(@RequestBody TarefaAssignmentDTO dto) {
        if (dto.alunoIds() == null || dto.alunoIds().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nenhum aluno selecionado."));
        }
        
        int count = 0;
        for (Long alunoId : dto.alunoIds()) {
            Usuario aluno = usuarioRepository.findById(alunoId).orElse(null);
            if (aluno != null) {
                Tarefa t = new Tarefa();
                t.setTipo(dto.tipo());
                t.setConteudo(dto.conteudo());
                t.setDataCriacao(LocalDateTime.now());
                t.setAluno(aluno);
                tarefaRepository.save(t);
                count++;
            }
        }
        return ResponseEntity.ok(Map.of("message", String.format("Tarefa definida para %d aluno(s)!", count)));
    }

    @GetMapping("/tarefa-atual")
    public ResponseEntity<?> getTarefaAtual() {
        Optional<Tarefa> ultima = tarefaRepository.findTopByOrderByDataCriacaoDesc(); 
        if (ultima.isEmpty()) {
            return ResponseEntity.ok(Map.of("tipo", "LIVRE", "conteudo", ""));
        }
        Tarefa t = ultima.get();
        return ResponseEntity.ok(Map.of("tipo", t.getTipo(), "conteudo", t.getConteudo()));
    }

    // --- CÁLCULO DE PENDÊNCIAS (CORRIGIDO PARA LIVRE) ---
    @GetMapping("/pendentes")
    public ResponseEntity<?> getTarefasPendentes(@RequestHeader("x-child-id") Long childId) {
        // Busca tarefas e atividades
        List<Tarefa> tarefasAtribuidas = tarefaRepository.findByAlunoIdOrderByDataCriacaoDesc(childId); 
        List<Atividade> atividadesFeitas = atividadeRepository.findByAlunoIdOrderByDataRealizacaoDesc(childId);

        // Agrupa atividades feitas (normalizando texto)
        Map<String, Long> contagemFeitas = atividadesFeitas.stream()
            .collect(Collectors.groupingBy(
                a -> {
                    // CORREÇÃO: Se for LIVRE, ignoramos o conteúdo para criar uma chave genérica.
                    // Isso permite que a Tarefa LIVRE (sem conteúdo) bata com a Atividade LIVRE (com desenho).
                    if ("LIVRE".equalsIgnoreCase(a.getTipo())) {
                        return "LIVRE|GENERICO";
                    }
                    String c = a.getConteudo() == null ? "" : a.getConteudo().trim().toUpperCase();
                    return a.getTipo() + "|" + c;
                },
                Collectors.counting()
            ));

        List<Tarefa> pendentes = new ArrayList<>();

        // Abate as feitas das atribuídas
        for (Tarefa tarefa : tarefasAtribuidas) {
            String chave;
            
            // CORREÇÃO: Gerar a mesma chave genérica para a tarefa LIVRE
            if ("LIVRE".equalsIgnoreCase(tarefa.getTipo())) {
                chave = "LIVRE|GENERICO";
            } else {
                String conteudo = tarefa.getConteudo() == null ? "" : tarefa.getConteudo().trim().toUpperCase();
                chave = tarefa.getTipo() + "|" + conteudo;
            }
            
            if (contagemFeitas.containsKey(chave) && contagemFeitas.get(chave) > 0) {
                contagemFeitas.put(chave, contagemFeitas.get(chave) - 1);
            } else {
                pendentes.add(tarefa);
            }
        }
        return ResponseEntity.ok(pendentes);
    }
    
    // --- CONTAGEM ESPECÍFICA DE TAREFAS "LIVRE" ---
    @GetMapping("/contar-tarefas-livre/{alunoId}")
    public ResponseEntity<?> contarTarefasLivreAtribuidas(@PathVariable Long alunoId) {
        List<Tarefa> tarefas = tarefaRepository.findByAlunoIdOrderByDataCriacaoDesc(alunoId);
        long count = tarefas.stream().filter(t -> "LIVRE".equalsIgnoreCase(t.getTipo())).count();
        return ResponseEntity.ok(Map.of("total", count));
    }

    // --- ENDPOINTS GERAIS ---
    @GetMapping("/total-enviadas-global")
    public ResponseEntity<?> getTotalEnviadasGlobal() {
        long count = tarefaRepository.count(); 
        return ResponseEntity.ok(Map.of("total", count));
    }
    
    @GetMapping("/total-atribuidas/{childId}")
    public ResponseEntity<?> getTotalAtribuidas(@PathVariable Long childId) {
        long count = tarefaRepository.countByAlunoId(childId); 
        return ResponseEntity.ok(Map.of("total", count));
    }

    // --- GERENCIAMENTO DE TAREFAS (PROFESSOR) ---
    @GetMapping("/tarefas-recentes")
    public ResponseEntity<List<Tarefa>> listarTarefasRecentes() {
        List<Tarefa> todas = tarefaRepository.findAll();
        todas.sort((t1, t2) -> t2.getDataCriacao().compareTo(t1.getDataCriacao()));
        if(todas.size() > 50) todas = todas.subList(0, 50);
        return ResponseEntity.ok(todas);
    }

    @DeleteMapping("/tarefas/{id}")
    public ResponseEntity<?> excluirTarefa(@PathVariable Long id) {
        if(tarefaRepository.existsById(id)) {
            tarefaRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Tarefa excluída com sucesso!"));
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/tarefas/{id}")
    public ResponseEntity<?> atualizarTarefa(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        return tarefaRepository.findById(id).map(t -> {
            if(payload.containsKey("conteudo")) t.setConteudo(payload.get("conteudo"));
            if(payload.containsKey("tipo")) t.setTipo(payload.get("tipo"));
            tarefaRepository.save(t);
            return ResponseEntity.ok(Map.of("message", "Tarefa atualizada!"));
        }).orElse(ResponseEntity.notFound().build());
    }
}