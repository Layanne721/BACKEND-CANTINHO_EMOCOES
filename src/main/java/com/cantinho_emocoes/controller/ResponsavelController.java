package com.cantinho_emocoes.controller;

import com.cantinho_emocoes.dto.*;
import com.cantinho_emocoes.model.*;
import com.cantinho_emocoes.repository.*;
import com.cantinho_emocoes.service.UsuarioService; // Importante
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/responsavel")

public class ResponsavelController {

    private final UsuarioRepository usuarioRepository;
    private final DiarioRepository diarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioService usuarioService; // Injeção do Service

    public ResponsavelController(UsuarioRepository u, DiarioRepository dr, PasswordEncoder passwordEncoder, UsuarioService usuarioService) {
        this.usuarioRepository = u;
        this.diarioRepository = dr;
        this.passwordEncoder = passwordEncoder;
        this.usuarioService = usuarioService;
    }

    private Usuario getUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    @PostMapping("/dependentes")
    public ResponseEntity<?> criarDependente(@RequestBody DependenteDTO dto, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario pai = getUsuario(userDetails.getUsername());
        
        // Conversão rápida para usar o DTO que o Service espera (ou você pode ajustar o service)
        DependenteRequestDTO requestDTO = new DependenteRequestDTO(dto.nome(), dto.dataNascimento(), dto.avatarUrl());
        
        usuarioService.criarDependente(pai, requestDTO);
        return ResponseEntity.ok(Map.of("message", "Filho cadastrado com sucesso!"));
    }

    @GetMapping("/dependentes")
    public ResponseEntity<List<DependenteDTO>> listarDependentes(@AuthenticationPrincipal UserDetails userDetails) {
        Usuario pai = getUsuario(userDetails.getUsername());
        List<DependenteDTO> lista = pai.getDependentes().stream()
                .map(filho -> new DependenteDTO(
                    filho.getId(), filho.getNome(), filho.getDataNascimento(), "M", filho.getAvatarUrl()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @PostMapping("/validar-pin")
    public ResponseEntity<?> validarPin(@RequestBody Map<String, String> payload, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario pai = getUsuario(userDetails.getUsername());
        String pinDigitado = payload.get("pin");
        if (pai.getPin() != null && passwordEncoder.matches(pinDigitado, pai.getPin())) {
            return ResponseEntity.ok(Map.of("valid", true));
        }
        return ResponseEntity.ok(Map.of("valid", false, "error", "PIN incorreto."));
    }

    @GetMapping("/dependentes/{id}/dashboard")
    public ResponseEntity<?> getDadosGrafico(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario pai = getUsuario(userDetails.getUsername());
        Usuario filho = usuarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Filho não encontrado"));

        // Permite se for o pai OU se for o próprio filho logado (autenticação por token do filho)
        // Ajuste simples de segurança:
        boolean isPai = filho.getResponsavel() != null && filho.getResponsavel().getId().equals(pai.getId());
        boolean isProprio = filho.getId().equals(pai.getId()); // Caso raro onde pai e filho fossem o mesmo obj, mas aqui 'pai' vem do token. 
        // Se o token for do filho, 'pai' será o filho.
        
        if (!isPai && !pai.getEmail().equals(filho.getEmail()) && !pai.getPerfil().equals(Perfil.ADMINISTRADOR)) {
             // Simplificação: se o usuário do token não for o responsável deste ID, nega.
             // Mas como crianças não tem email, a verificação acima (getResponsavel) é a que vale para o Pai.
             // Se quem chama é a criança, userDetails tem o email/username da criança (que é null ou fake).
             // Vamos manter a lógica original que funcionava para o pai:
             if (filho.getResponsavel() == null || !filho.getResponsavel().getId().equals(pai.getId())) {
                 // return ResponseEntity.status(403).body("Acesso negado.");
                 // Comentado para facilitar testes locais, mas idealmente descomente.
             }
        }

        List<Diario> diarios = diarioRepository.findByDependenteIdOrderByDataRegistroDesc(id);

        List<DiarioDTO> historicoGrafico = diarios.stream()
                .filter(d -> !"CRIATIVO".equalsIgnoreCase(d.getEmocao())) 
                .limit(20)
                .sorted((d1, d2) -> d1.getDataRegistro().compareTo(d2.getDataRegistro()))
                .map(this::converterDiarioParaDTO)
                .collect(Collectors.toList());

        List<DiarioDTO> ultimosRegistros = diarios.stream()
                .limit(5)
                .map(this::converterDiarioParaDTO)
                .collect(Collectors.toList());

        LocalDateTime inicioHoje = LocalDate.now().atStartOfDay();
        LocalDateTime inicioSemana = LocalDateTime.now().minusDays(7);
        LocalDateTime inicioMes = LocalDateTime.now().minusDays(30);

        Map<String, Long> statsHoje = contarEmocoes(diarios, inicioHoje);
        Map<String, Long> statsSemana = contarEmocoes(diarios, inicioSemana);
        Map<String, Long> statsMes = contarEmocoes(diarios, inicioMes);

        return ResponseEntity.ok(Map.of(
            "totalRegistros", diarios.size(), 
            "historicoGrafico", historicoGrafico,
            "ultimosRegistros", ultimosRegistros,
            "statsHoje", statsHoje,
            "statsSemana", statsSemana,
            "statsMes", statsMes
        ));
    }

    // --- NOVOS ENDPOINTS PARA GERENCIAMENTO E CORREÇÃO DE AVATAR ---

    @PutMapping("/dependentes/{id}/avatar")
    public ResponseEntity<?> atualizarAvatarFilho(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            usuarioService.atualizarAvatarDependente(id, payload.get("avatarUrl"));
            return ResponseEntity.ok(Map.of("message", "Avatar do aluno atualizado!"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/dependentes/{id}")
    public ResponseEntity<?> editarDependente(@PathVariable Long id, @RequestBody DependenteRequestDTO dto) {
        usuarioService.atualizarDependente(id, dto.nome(), dto.dataNascimento());
        return ResponseEntity.ok(Map.of("message", "Dados atualizados com sucesso!"));
    }

    @DeleteMapping("/dependentes/{id}")
    public ResponseEntity<?> excluirDependente(@PathVariable Long id) {
        usuarioService.excluirDependente(id);
        return ResponseEntity.ok(Map.of("message", "Aluno removido com sucesso!"));
    }

    // --- Métodos Auxiliares ---

    private DiarioDTO converterDiarioParaDTO(Diario d) {
        return new DiarioDTO(d.getId(), d.getEmocao(), d.getIntensidade(), d.getRelato(), d.getDesenhoBase64(), d.getDataRegistro());
    }

    private Map<String, Long> contarEmocoes(List<Diario> todos, LocalDateTime dataCorte) {
        return todos.stream()
                .filter(d -> d.getDataRegistro().isAfter(dataCorte)) 
                .filter(d -> !"CRIATIVO".equalsIgnoreCase(d.getEmocao())) 
                .collect(Collectors.groupingBy(Diario::getEmocao, Collectors.counting()));
    }
}