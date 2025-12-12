package com.cantinho_emocoes.controller;

import com.cantinho_emocoes.dto.AdminUsuarioDTO;
import com.cantinho_emocoes.service.BackupService;
import com.cantinho_emocoes.service.UsuarioService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UsuarioService usuarioService;
    private final BackupService backupService;

    // Injeção de dependência via construtor
    public AdminController(UsuarioService usuarioService, BackupService backupService) {
        this.usuarioService = usuarioService;
        this.backupService = backupService;
    }

    // --- Endpoints de Usuários (Mantidos) ---

    @GetMapping("/usuarios")
    public ResponseEntity<List<AdminUsuarioDTO>> listarTodos() {
        List<AdminUsuarioDTO> usuarios = usuarioService.listarTodosUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> excluirUsuario(@PathVariable Long id) {
        try {
            usuarioService.deletarUsuarioPeloAdmin(id);
            return ResponseEntity.ok(Map.of("message", "Usuário excluído com sucesso!"));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // --- Novos Endpoints de Backup e Restore ---

    @GetMapping("/backup")
    public ResponseEntity<Resource> downloadBackup() {
        try {
            File backupFile = backupService.gerarBackup();
            FileSystemResource resource = new FileSystemResource(backupFile);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + backupFile.getName());
            headers.add(HttpHeaders.CONTENT_TYPE, "application/sql");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(backupFile.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build(); // Retorna 500 se falhar
        }
    }

    @PostMapping("/restore")
    public ResponseEntity<?> restoreBackup(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Arquivo inválido ou vazio."));
        }

        // Validação da extensão (.sql, .dum, .dump)
        String fileName = file.getOriginalFilename();
        if (fileName == null || 
           (!fileName.endsWith(".sql") && !fileName.endsWith(".dum") && !fileName.endsWith(".dump"))) {
             return ResponseEntity.badRequest().body(Map.of("error", "Formato não suportado. Use .sql, .dum ou .dump"));
        }

        try {
            backupService.restaurarBackup(file);
            return ResponseEntity.ok(Map.of("message", "Banco de dados restaurado com sucesso!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao restaurar: " + e.getMessage()));
        }
    }
}