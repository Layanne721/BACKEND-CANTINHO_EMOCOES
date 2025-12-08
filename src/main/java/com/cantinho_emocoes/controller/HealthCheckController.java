package com.cantinho_emocoes.controller; // Ajuste o pacote conforme sua estrutura

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api")


public class HealthCheckController {

    @Autowired
    private JdbcTemplate jdbcTemplate; // Spring irá injetar a conexão com o BD aqui

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> checkHealth() {
        Map<String, String> response = new HashMap<>();
        try {
            // Tenta executar uma consulta SQL simples para verificar a conexão com o banco
            jdbcTemplate.execute("SELECT 1");
            response.put("status", "ok");
            response.put("message", "Backend e Banco de Dados estão online!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Se houver qualquer erro, a conexão com o banco falhou
            response.put("status", "error");
            response.put("message", "Backend online, mas a conexão com o Banco de Dados falhou. Detalhes: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
