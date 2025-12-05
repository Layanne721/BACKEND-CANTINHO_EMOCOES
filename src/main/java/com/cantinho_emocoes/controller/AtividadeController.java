package com.cantinho_emocoes.controller;

import com.cantinho_emocoes.model.Atividade;
import com.cantinho_emocoes.model.Usuario;
import com.cantinho_emocoes.repository.AtividadeRepository;
import com.cantinho_emocoes.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/atividades")
@CrossOrigin(origins = "http://localhost:5173")
public class AtividadeController {

    private final AtividadeRepository atividadeRepository;
    private final UsuarioRepository usuarioRepository;

    // Simulação em memória da "Atividade do Dia"
    public static Map<String, String> ULTIMA_TAREFA = new HashMap<>();
    
    // NOVO: Contador de quantas atividades foram enviadas no total (simulado em memória)
    // Em produção, isso seria uma contagem no banco de dados de tarefas criadas.
    public static int TOTAL_ATIVIDADES_ENVIADAS = 0;

    public AtividadeController(AtividadeRepository atividadeRepository, UsuarioRepository usuarioRepository) {
        this.atividadeRepository = atividadeRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // --- ALUNO: SALVAR DESENHO FEITO ---
    @PostMapping
    public ResponseEntity<?> salvarAtividadeFeita(@RequestHeader("x-child-id") Long childId, @RequestBody Atividade atividade) {
        Usuario aluno = usuarioRepository.findById(childId).orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
        atividade.setAluno(aluno);
        atividade.setDataRealizacao(LocalDateTime.now());
        atividadeRepository.save(atividade);
        return ResponseEntity.ok(Map.of("message", "Atividade entregue com sucesso!"));
    }

    // --- PROFESSOR: LISTAR ATIVIDADES FEITAS PELO ALUNO ---
    @GetMapping("/aluno/{childId}")
    public ResponseEntity<List<Atividade>> listarAtividadesDoAluno(@PathVariable Long childId) {
        return ResponseEntity.ok(atividadeRepository.findByAlunoIdOrderByDataRealizacaoDesc(childId));
    }

    // --- PROFESSOR: ENVIAR NOVA TAREFA (DEFINE A "Lousa") ---
    @PostMapping("/definir-tarefa")
    public ResponseEntity<?> definirTarefa(@RequestBody Map<String, String> payload) {
        ULTIMA_TAREFA = payload;
        
        // Incrementa o contador apenas se não for tarefa repetida ou vazia (lógica simplificada)
        // Aqui assumimos que cada clique em "Enviar" é uma nova tarefa valendo nota/presença
        TOTAL_ATIVIDADES_ENVIADAS++;
        
        return ResponseEntity.ok(Map.of("message", "Tarefa definida para a turma!", "totalEnviadas", TOTAL_ATIVIDADES_ENVIADAS));
    }

    // --- ALUNO: BUSCAR TAREFA DO DIA ---
    @GetMapping("/tarefa-atual")
    public ResponseEntity<?> getTarefaAtual() {
        if (ULTIMA_TAREFA.isEmpty()) {
            return ResponseEntity.ok(Map.of("tipo", "LIVRE", "conteudo", ""));
        }
        return ResponseEntity.ok(ULTIMA_TAREFA);
    }
    
    // --- NOVO ENDPOINT: RETORNA O TOTAL DE ATIVIDADES QUE O PROFESSOR JÁ ENVIOU ---
    @GetMapping("/total-enviadas")
    public ResponseEntity<?> getTotalEnviadas() {
        return ResponseEntity.ok(Map.of("total", TOTAL_ATIVIDADES_ENVIADAS));
    }
    
    // --- UTILITÁRIO: RESETAR CONTADOR (OPCIONAL, PARA TESTES) ---
    @PostMapping("/resetar-contador")
    public ResponseEntity<?> resetarContador() {
        TOTAL_ATIVIDADES_ENVIADAS = 0;
        ULTIMA_TAREFA.clear();
        return ResponseEntity.ok(Map.of("message", "Contador resetado com sucesso."));
    }
}