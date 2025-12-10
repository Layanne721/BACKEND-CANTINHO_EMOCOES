package com.cantinho_emocoes.controller;

import com.cantinho_emocoes.dto.*;
import com.cantinho_emocoes.model.Perfil;
import com.cantinho_emocoes.model.Usuario;
import com.cantinho_emocoes.repository.UsuarioRepository;
import com.cantinho_emocoes.security.JwtService;
import com.cantinho_emocoes.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    public AuthController(AuthenticationManager authenticationManager, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService, UsuarioService usuarioService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerResponsavel(@RequestBody RegisterRequestDTO request) {
        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email já está em uso."));
        }
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(request.nome());
        novoUsuario.setEmail(request.email());
        novoUsuario.setSenha(passwordEncoder.encode(request.senha()));
        
        if (request.pin() == null || request.pin().length() != 4) {
            return ResponseEntity.badRequest().body(Map.of("message", "O PIN deve ter 4 dígitos."));
        }
        
        novoUsuario.setPin(passwordEncoder.encode(request.pin()));
        novoUsuario.setPerfil(Perfil.RESPONSAVEL);
        novoUsuario.setAvatarUrl(request.avatarUrl());
        novoUsuario.setDataCadastro(LocalDate.now());
        usuarioRepository.save(novoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Conta criada com sucesso!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha())
            );
            Usuario usuario = (Usuario) authentication.getPrincipal();
            String token = jwtService.generateToken(usuario);
            return ResponseEntity.ok(new LoginResponseDTO(token, usuario.getNome(), usuario.getEmail(), usuario.getPerfil(), usuario.getAvatarUrl()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Email ou senha inválidos."));
        }
    }

    @PostMapping("/validar-pin")
    public ResponseEntity<?> validarPin(@RequestBody Map<String, String> request, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        String pinDigitado = request.get("pin");
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        
        if (passwordEncoder.matches(pinDigitado, usuario.getPin())) {
            return ResponseEntity.ok(Map.of("message", "PIN válido!"));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "PIN incorreto."));
        }
    }

    // --- NOVA ROTA UNIFICADA PARA CORRIGIR O ERRO 500 ---
    // DTO local para capturar tanto 'name' quanto 'nome' e 'avatarUrl'
    public record PerfilCompletoDTO(String name, String nome, String avatarUrl) {}

    @PutMapping("/meu-perfil")
    public ResponseEntity<?> updatePerfilCompleto(@AuthenticationPrincipal UserDetails userDetails, @RequestBody PerfilCompletoDTO request) {
        if (userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        
        try {
            String email = userDetails.getUsername();
            String nomeAtualizado = "";

            // 1. Atualiza Nome (verifica se veio como 'nome' ou 'name')
            String novoNome = request.nome() != null ? request.nome() : request.name();
            if (novoNome != null && !novoNome.isBlank()) {
                usuarioService.atualizarNome(email, novoNome);
                nomeAtualizado = novoNome;
            }

            // 2. Atualiza Avatar
            if (request.avatarUrl() != null && !request.avatarUrl().isBlank()) {
                usuarioService.atualizarAvatar(email, request.avatarUrl());
            }

            return ResponseEntity.ok(Map.of(
                "message", "Perfil atualizado com sucesso!",
                "user", Map.of(
                    "name", nomeAtualizado,
                    "avatarUrl", request.avatarUrl() != null ? request.avatarUrl() : ""
                )
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erro ao processar atualização: " + e.getMessage()));
        }
    }
    // ----------------------------------------------------

    @PutMapping("/meu-perfil/dados")
    public ResponseEntity<?> updateMeusDados(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UsuarioUpdateDTO updateDTO) {
        if (userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            usuarioService.atualizarNome(userDetails.getUsername(), updateDTO.getNome());
            return ResponseEntity.ok(Map.of("message", "Nome atualizado com sucesso!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Erro ao atualizar."));
        }
    }

    @DeleteMapping("/meu-perfil")
    public ResponseEntity<?> deleteAccount(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            usuarioService.excluirContaFamilia(userDetails.getUsername());
            return ResponseEntity.ok(Map.of("message", "Conta excluída com sucesso."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro ao excluir conta: " + e.getMessage()));
        }
    }

    @PostMapping("/recuperar-senha")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            usuarioService.requestPasswordReset(request.getEmail());
            return ResponseEntity.ok(Map.of("message", "Se o e-mail existir, o link foi enviado."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erro ao enviar e-mail: " + e.getMessage()));
        }
    }

    @PostMapping("/resetar-senha")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            usuarioService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Senha alterada com sucesso!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/meu-perfil/avatar")
    public ResponseEntity<?> updateAvatar(@AuthenticationPrincipal UserDetails userDetails, @RequestBody AvatarSelectionDTO avatarDTO) {
        if (userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            usuarioService.atualizarAvatar(userDetails.getUsername(), avatarDTO.getAvatarUrl());
            return ResponseEntity.ok(Map.of(
                "message", "Avatar atualizado com sucesso!",
                "avatarUrl", avatarDTO.getAvatarUrl()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Erro ao atualizar avatar."));
        }
    }
}