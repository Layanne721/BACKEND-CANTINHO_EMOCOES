package com.cantinho_emocoes.config;

import com.cantinho_emocoes.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(AuthenticationProvider authenticationProvider, JwtAuthFilter jwtAuthFilter) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Desabilita CSRF (API Stateless)
            .csrf(AbstractHttpConfigurer::disable)
            
            // Ativa o CORS usando a configuração definida abaixo
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Sessão Stateless (Sem guardar estado no servidor)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Permissões de Rotas
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/auth/**", "/api/health", "/uploads/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/api/responsavel/**").hasRole("RESPONSAVEL")
                .requestMatchers("/api/diario/**").authenticated()
                .anyRequest().authenticated()
            )
            
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // --- MUDANÇA PRINCIPAL AQUI ---
        // Usamos setAllowedOriginPatterns("*") para liberar QUALQUER origem (Render, Local, Celular)
        configuration.setAllowedOriginPatterns(List.of("*"));
        
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Libera todos os headers (Authorization, Content-Type, e os personalizados x-child-id)
        configuration.setAllowedHeaders(List.of("*"));
        
        // Permite credenciais (cookies/tokens) mesmo com o wildcard "*" (graças ao OriginPatterns)
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}