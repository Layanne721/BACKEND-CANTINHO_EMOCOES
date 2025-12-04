package com.cantinho_emocoes.controller;

import com.cantinho_emocoes.dto.*;
import com.cantinho_emocoes.model.*;
import com.cantinho_emocoes.repository.*;
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
@CrossOrigin(origins = "http://localhost:5173")
public class ResponsavelController {

    private final UsuarioRepository usuarioRepository;
    private final DiarioRepository diarioRepository;
    private final PasswordEncoder passwordEncoder;

    public ResponsavelController(UsuarioRepository u, DiarioRepository dr, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = u;
        this.diarioRepository = dr;
        this.passwordEncoder = passwordEncoder;
    }

    private Usuario getUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    @PostMapping("/dependentes")
    public ResponseEntity<?> criarDependente(@RequestBody DependenteDTO dto, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario pai = getUsuario(userDetails.getUsername());
        Usuario filho = new Usuario();
        filho.setNome(dto.nome());
        filho.setDataNascimento(dto.dataNascimento());
        filho.setAvatarUrl(dto.avatarUrl());
        filho.setPerfil(Perfil.CRIANCA);
        filho.setResponsavel(pai);
        filho.setDataCadastro(LocalDate.now());
        filho.setSenha(passwordEncoder.encode(UUID.randomUUID().toString()));
        usuarioRepository.save(filho);
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

    // --- AQUI ESTÁ A ATUALIZAÇÃO PRINCIPAL ---
    @GetMapping("/dependentes/{id}/dashboard")
    public ResponseEntity<?> getDadosGrafico(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario pai = getUsuario(userDetails.getUsername());
        Usuario filho = usuarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Filho não encontrado"));

        if (filho.getResponsavel() == null || !filho.getResponsavel().getId().equals(pai.getId())) {
            return ResponseEntity.status(403).body("Acesso negado.");
        }

        List<Diario> diarios = diarioRepository.findByDependenteIdOrderByDataRegistroDesc(id);

        // 1. Histórico para o Gráfico (sem desenhos)
        List<DiarioDTO> historicoGrafico = diarios.stream()
                .filter(d -> !"CRIATIVO".equalsIgnoreCase(d.getEmocao())) 
                .limit(20)
                .sorted((d1, d2) -> d1.getDataRegistro().compareTo(d2.getDataRegistro()))
                .map(this::converterDiarioParaDTO)
                .collect(Collectors.toList());

        // 2. Últimos Registros Gerais
        List<DiarioDTO> ultimosRegistros = diarios.stream()
                .limit(5)
                .map(this::converterDiarioParaDTO)
                .collect(Collectors.toList());

        // 3. ESTATÍSTICAS (HOJE, SEMANA, MÊS)
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
            "statsHoje", statsHoje,     // Novo
            "statsSemana", statsSemana, // Novo
            "statsMes", statsMes        // Novo
        ));
    }

    // Método auxiliar para converter entidade para DTO
    private DiarioDTO converterDiarioParaDTO(Diario d) {
        return new DiarioDTO(d.getId(), d.getEmocao(), d.getIntensidade(), d.getRelato(), d.getDesenhoBase64(), d.getDataRegistro());
    }

    // Método auxiliar para contar emoções em um período
    private Map<String, Long> contarEmocoes(List<Diario> todos, LocalDateTime dataCorte) {
        return todos.stream()
                .filter(d -> d.getDataRegistro().isAfter(dataCorte)) // Filtra pela data
                .filter(d -> !"CRIATIVO".equalsIgnoreCase(d.getEmocao())) // Ignora desenhos na contagem
                .collect(Collectors.groupingBy(Diario::getEmocao, Collectors.counting()));
    }
}