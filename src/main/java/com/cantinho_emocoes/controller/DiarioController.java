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
}