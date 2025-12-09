package com.cantinho_emocoes.controller;

import com.cantinho_emocoes.model.Diario;
import com.cantinho_emocoes.model.Usuario;
import com.cantinho_emocoes.repository.DiarioRepository;
import com.cantinho_emocoes.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException; // Importação necessária
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diario")
// @CrossOrigin REMOVIDO
public class DiarioController {

    private final DiarioRepository diarioRepository;
    private final UsuarioRepository usuarioRepository;

    public DiarioController(DiarioRepository diarioRepository, UsuarioRepository usuarioRepository) {
        this.diarioRepository = diarioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/meus")
    public ResponseEntity<?> listarMeusDiarios(
            @RequestHeader("x-child-id") Long childId, 
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (childId == null) return ResponseEntity.badRequest().body("ID da criança não informado.");
        
        List<Diario> diarios = diarioRepository.findByDependenteIdOrderByDataRegistroDesc(childId);
        return ResponseEntity.ok(diarios);
    }

    @PostMapping
    public ResponseEntity<?> criarDiario(
            @RequestHeader("x-child-id") Long childId,
            @RequestBody Map<String, Object> payload) {

        Usuario crianca = usuarioRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Criança não encontrada"));

        Diario diario = new Diario();
        diario.setEmocao((String) payload.get("emocao"));
        diario.setIntensidade((Integer) payload.get("intensidade"));
        diario.setRelato((String) payload.get("relato"));
        
        if (payload.containsKey("desenhoBase64")) {
            diario.setDesenhoBase64((String) payload.get("desenhoBase64"));
        }

        diario.setDataRegistro(LocalDateTime.now());
        diario.setDependente(crianca);

        diarioRepository.save(diario);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Diário salvo com sucesso!"));
    }

    @PostMapping("/desenho")
    public ResponseEntity<?> salvarDesenho(
            @RequestHeader("x-child-id") Long childId,
            @RequestBody Map<String, String> payload) {

        Usuario crianca = usuarioRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Criança não encontrada"));

        String imagemBase64 = payload.get("imagem");

        Diario novoDiario = new Diario();
        
        novoDiario.setEmocao("CRIATIVO"); 
        
        novoDiario.setIntensidade(5); 
        novoDiario.setRelato("Atividade de Desenho"); 
        novoDiario.setDesenhoBase64(imagemBase64);
        novoDiario.setDataRegistro(LocalDateTime.now());
        novoDiario.setDependente(crianca);

        diarioRepository.save(novoDiario);

        return ResponseEntity.ok(Map.of("message", "Desenho salvo na galeria!"));
    }
    
    /**
     * Endpoint para atualizar campos de um registro de diário existente, como a data.
     */
    @PatchMapping("/{diarioId}")
    public ResponseEntity<?> atualizarDiario(
            @PathVariable Long diarioId,
            @RequestBody Map<String, Object> payload) {

        Diario diarioExistente = diarioRepository.findById(diarioId)
                .orElseThrow(() -> new RuntimeException("Registro de Diário não encontrado com ID: " + diarioId));

        if (payload.containsKey("emocao")) {
            diarioExistente.setEmocao((String) payload.get("emocao"));
        }
        
        if (payload.containsKey("intensidade")) {
            // A intensidade vem como Integer diretamente, não precisa de parse de String
            diarioExistente.setIntensidade((Integer) payload.get("intensidade"));
        }
        
        if (payload.containsKey("relato")) {
            diarioExistente.setRelato((String) payload.get("relato"));
        }
        
        // Permite a alteração da data (dataRegistro)
        if (payload.containsKey("dataRegistro")) {
             // A data deve ser enviada como String no formato ISO-8601 (ex: "2025-12-08T10:00:00")
             String novaDataStr = (String) payload.get("dataRegistro");
             try {
                 // Converte a string para LocalDateTime
                 LocalDateTime novaData = LocalDateTime.parse(novaDataStr);
                 diarioExistente.setDataRegistro(novaData);
             } catch (DateTimeParseException e) {
                 return ResponseEntity.badRequest().body(Map.of("error", "Formato de data inválido. Use ISO-8601 (ex: 2025-12-08T10:00:00)."));
             }
        }
        
        diarioRepository.save(diarioExistente);

        return ResponseEntity.ok(Map.of("message", "Registro de Diário atualizado com sucesso!"));
    }
}