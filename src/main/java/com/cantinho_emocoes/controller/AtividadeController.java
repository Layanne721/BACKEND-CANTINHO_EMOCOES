package com.cantinho_emocoes.controller;

import com.cantinho_emocoes.model.Atividade;
import com.cantinho_emocoes.model.Tarefa;
import com.cantinho_emocoes.model.Usuario;
import com.cantinho_emocoes.repository.AtividadeRepository;
import com.cantinho_emocoes.repository.TarefaRepository;
import com.cantinho_emocoes.repository.UsuarioRepository;
import com.cantinho_emocoes.dto.TarefaAssignmentDTO; // Importar o novo DTO
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/atividades")
// @CrossOrigin REMOVIDO
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

    /**
     * Define uma nova tarefa/atividade e a atribui a uma lista de alunos.
     * Agora, cria uma entrada de Tarefa individual para cada aluno selecionado.
     */
    @PostMapping("/definir-tarefa")
    public ResponseEntity<?> definirTarefa(@RequestBody TarefaAssignmentDTO dto) { // Usa o novo DTO
        
        if (dto.alunoIds() == null || dto.alunoIds().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nenhum aluno selecionado."));
        }
        
        int count = 0;
        for (Long alunoId : dto.alunoIds()) {
            Usuario aluno = usuarioRepository.findById(alunoId)
                    .orElse(null);

            if (aluno != null) {
                Tarefa t = new Tarefa();
                t.setTipo(dto.tipo());
                t.setConteudo(dto.conteudo());
                t.setDataCriacao(LocalDateTime.now());
                t.setAluno(aluno); // Associa a tarefa ao aluno
                
                tarefaRepository.save(t);
                count++;
            }
        }
        
        return ResponseEntity.ok(Map.of("message", String.format("Tarefa definida para %d aluno(s)!", count)));
    }

    /**
     * Retorna a "última" tarefa em geral (mantida para compatibilidade).
     */
    @GetMapping("/tarefa-atual")
    public ResponseEntity<?> getTarefaAtual() {
        // Pega a última tarefa CADASTRADA, ignorando o aluno, para simular o comportamento global anterior
        Optional<Tarefa> ultima = tarefaRepository.findTopByOrderByDataCriacaoDesc(); 
        if (ultima.isEmpty()) {
            return ResponseEntity.ok(Map.of("tipo", "LIVRE", "conteudo", ""));
        }
        // Retorna a tarefa, mas sem o vínculo do aluno para ser genérico
        Tarefa t = ultima.get();
        return ResponseEntity.ok(Map.of(
            "tipo", t.getTipo(), 
            "conteudo", t.getConteudo()
        ));
    }

    /**
     * Lista as tarefas que o aluno TEM que fazer (atribuídas e não completadas).
     */
    @GetMapping("/pendentes")
    public ResponseEntity<?> getTarefasPendentes(@RequestHeader("x-child-id") Long childId) {
        
        // 1. Busca todas as tarefas atribuídas a este aluno
        List<Tarefa> tarefasAtribuidas = tarefaRepository.findByAlunoIdOrderByDataCriacaoDesc(childId); 
        
        // 2. Busca todas as atividades COMPLETADAS por este aluno
        List<Atividade> atividadesFeitas = atividadeRepository.findByAlunoIdOrderByDataRealizacaoDesc(childId);

        // 3. Filtra as tarefas atribuídas que não foram completadas (Pendentes)
        List<Tarefa> pendentes = tarefasAtribuidas.stream()
            .filter(tarefa -> {
                // Tarefas "LIVRE" não contam como pendentes no painel
                if ("LIVRE".equalsIgnoreCase(tarefa.getTipo())) return false; 
                
                // Verifica se o aluno já completou uma atividade com o MESMO tipo e conteúdo
                boolean jaFez = atividadesFeitas.stream().anyMatch(a -> 
                    a.getTipo().equals(tarefa.getTipo()) && 
                    a.getConteudo().equals(tarefa.getConteudo())
                );
                return !jaFez;
            })
            .collect(Collectors.toList());

        // Retornamos a lista de Tarefas que estão pendentes para o aluno específico
        return ResponseEntity.ok(pendentes);
    }
    
    /**
     * RENOMEADO: Agora conta o total de atribuições feitas (global).
     */
    @GetMapping("/total-enviadas-global")
    public ResponseEntity<?> getTotalEnviadasGlobal() {
        // Conta todas as Tarefas (que agora são assignments, então este valor é o total de ATRIBUIÇÕES)
        long count = tarefaRepository.count(); 
        return ResponseEntity.ok(Map.of("total", count));
    }
    
    /**
     * NOVO ENDPOINT: Conta o total de tarefas ATRIBUÍDAS a um aluno específico.
     */
    @GetMapping("/total-atribuidas/{childId}")
    public ResponseEntity<?> getTotalAtribuidas(@PathVariable Long childId) {
        long count = tarefaRepository.countByAlunoId(childId); 
        return ResponseEntity.ok(Map.of("total", count));
    }
}