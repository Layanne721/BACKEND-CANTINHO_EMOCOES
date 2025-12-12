package com.cantinho_emocoes.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupService.class);

    // ✅ CONFIGURAÇÃO DO CAMINHO DO POSTGRES (SEU CAMINHO AQUI)
    // Nota: Usamos "\\" duplicado porque em Java a barra invertida é caractere de escape.
    private static final String PG_BIN_PATH = "C:\\pg18\\pgsql\\bin\\";

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    private Map<String, String> getDbInfo() {
        String cleanUrl = dbUrl.replace("jdbc:postgresql://", "");
        int queryParamIndex = cleanUrl.indexOf("?");
        if (queryParamIndex != -1) {
            cleanUrl = cleanUrl.substring(0, queryParamIndex);
        }
        
        String[] hostPortDb = cleanUrl.split("/");
        String[] hostPort = hostPortDb[0].split(":");
        String dbName = hostPortDb.length > 1 ? hostPortDb[1] : "cantinho";

        return Map.of(
            "host", hostPort[0],
            "port", hostPort.length > 1 ? hostPort[1] : "5432",
            "db", dbName
        );
    }

    public File gerarBackup() throws IOException, InterruptedException {
        Map<String, String> info = getDbInfo();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File backupFile = File.createTempFile("backup_cantinho_" + timestamp + "_", ".sql");

        // ✅ ALTERADO: Usa o caminho completo do executável
        String pgDumpExe = PG_BIN_PATH + "pg_dump.exe";

        ProcessBuilder pb = new ProcessBuilder(
            pgDumpExe,
            "-h", info.get("host"),
            "-p", info.get("port"),
            "-U", dbUser,
            "--no-owner",
            "--no-acl",
            "--clean",
            "--if-exists",
            "-f", backupFile.getAbsolutePath(),
            info.get("db")
        );

        pb.environment().put("PGPASSWORD", dbPassword);
        pb.redirectErrorStream(true);

        log.info("Iniciando backup usando: {}", pgDumpExe);
        
        Process process = pb.start();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.contains("pg_dump: warning")) {
                    log.info("Backup Output: {}", line);
                }
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Erro ao gerar backup. Código de saída: " + exitCode);
        }

        return backupFile;
    }

    public void restaurarBackup(MultipartFile file) throws IOException, InterruptedException {
        Map<String, String> info = getDbInfo();
        
        Path tempFile = Files.createTempFile("restore_", ".sql");
        file.transferTo(tempFile.toFile());

        // ✅ ALTERADO: Usa o caminho completo do executável
        String psqlExe = PG_BIN_PATH + "psql.exe";

        ProcessBuilder pb = new ProcessBuilder(
            psqlExe,
            "-h", info.get("host"),
            "-p", info.get("port"),
            "-U", dbUser,
            "-d", info.get("db"),
            "-f", tempFile.toAbsolutePath().toString()
        );

        pb.environment().put("PGPASSWORD", dbPassword);
        pb.redirectErrorStream(true);

        log.info("Iniciando restauração usando: {}", psqlExe);

        Process process = pb.start();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("Restore Output: {}", line);
            }
        }

        int exitCode = process.waitFor();
        Files.deleteIfExists(tempFile);

        if (exitCode != 0) {
            throw new RuntimeException("Erro ao restaurar backup. Código: " + exitCode);
        }
    }
}