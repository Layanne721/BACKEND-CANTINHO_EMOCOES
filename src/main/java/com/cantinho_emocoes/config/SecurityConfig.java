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
            // Desabilita CSRF (necessário para APIs Stateless como a sua)
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configura o CORS usando o método corsConfigurationSource() abaixo
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Define que não haverá sessão no servidor (Statefull), pois usamos JWT
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configura as permissões de acesso
            .authorizeHttpRequests(authorize -> authorize
                // LIBERA GERAL: Login, Cadastro, Health Check e Imagens
                .requestMatchers("/auth/**", "/api/health", "/uploads/**").permitAll()
                
                // REGRAS DE ACESSO
                .requestMatchers("/api/admin/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/api/responsavel/**").hasRole("RESPONSAVEL")
                .requestMatchers("/api/diario/**").authenticated() 
                
                // Qualquer outra coisa precisa estar logado
                .anyRequest().authenticated()
            )
            
            // Adiciona o seu filtro de Token JWT antes do filtro padrão do Spring
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // --- AQUI ESTAVA O PROBLEMA ---
        // Adicionei as portas 5174 e 5175 para garantir que funcione se o Vite mudar a porta
        configuration.setAllowedOrigins(List.of(
            "http://localhost:5173", 
            "http://localhost:5174", 
            "http://localhost:5175"
        ));
        
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "x-auth-token", "x-child-id"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}