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
import java.util.Objects;

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
     */
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

    /**
     * Retorna a "última" tarefa em geral (mantida para compatibilidade).
     */
    @GetMapping("/tarefa-atual")
    public ResponseEntity<?> getTarefaAtual() {
        Optional<Tarefa> ultima = tarefaRepository.findTopByOrderByDataCriacaoDesc(); 
        if (ultima.isEmpty()) {
            return ResponseEntity.ok(Map.of("tipo", "LIVRE", "conteudo", ""));
        }
        Tarefa t = ultima.get();
        return ResponseEntity.ok(Map.of(
            "tipo", t.getTipo(), 
            "conteudo", t.getConteudo()
        ));
    }

    /**
     * Lista as tarefas que o aluno TEM que fazer (Pendentes).
     * CORREÇÃO: Usa contagem para garantir que se o prof mandou 2 atividades iguais,
     * o aluno tenha que fazer 2 vezes.
     */
    @GetMapping("/pendentes")
    public ResponseEntity<?> getTarefasPendentes(@RequestHeader("x-child-id") Long childId) {
        
        // 1. Busca todas as tarefas atribuídas a este aluno (da mais recente para a mais antiga)
        List<Tarefa> tarefasAtribuidas = tarefaRepository.findByAlunoIdOrderByDataCriacaoDesc(childId); 
        
        // 2. Busca todas as atividades COMPLETADAS por este aluno
        List<Atividade> atividadesFeitas = atividadeRepository.findByAlunoIdOrderByDataRealizacaoDesc(childId);

        // 3. Cria um mapa de contagem das atividades feitas: "TIPO|CONTEUDO" -> Quantidade
        Map<String, Long> contagemFeitas = atividadesFeitas.stream()
            .collect(Collectors.groupingBy(
                a -> a.getTipo() + "|" + (a.getConteudo() == null ? "" : a.getConteudo()),
                Collectors.counting()
            ));

        List<Tarefa> pendentes = new ArrayList<>();

        // 4. Itera sobre as atribuições e "abate" do saldo de feitas
        for (Tarefa tarefa : tarefasAtribuidas) {
            String chave = tarefa.getTipo() + "|" + (tarefa.getConteudo() == null ? "" : tarefa.getConteudo());
            
            if (contagemFeitas.containsKey(chave) && contagemFeitas.get(chave) > 0) {
                // Se existe uma realização sobrando para este tipo de tarefa, usamos ela para "quitar" esta pendência
                contagemFeitas.put(chave, contagemFeitas.get(chave) - 1);
            } else {
                // Se não tem saldo (o aluno não fez vezes suficientes), adiciona na lista de pendentes
                pendentes.add(tarefa);
            }
        }

        return ResponseEntity.ok(pendentes);
    }
    
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
}