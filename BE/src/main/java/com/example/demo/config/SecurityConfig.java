package com.example.demo.config;

import com.example.demo.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /*
      BCrypt non e' un semplice hash: incorpora un "sale" casuale
      diverso per ogni password e un costo di calcolo volutamente
      alto. Due utenti con la stessa password ottengono hash diversi,
      e provare le password a tentativi diventa lentissimo.
    */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Il CORS va abilitato anche qui: Spring Security gira
                // prima di WebMvcConfigurer, e senza questa riga
                // bloccherebbe le richieste dal frontend prima ancora
                // che la configurazione CORS entri in gioco.
                .cors(cors -> {})
                // CSRF protegge le sessioni basate su cookie. Qui
                // l'autenticazione viaggia in un'intestazione, quindi
                // non serve e bloccherebbe solo le POST.
                .csrf(AbstractHttpConfigurer::disable)
                // Nessuna sessione sul server: ogni richiesta porta
                // con se' il proprio token.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Registrazione e login devono essere pubblici,
                        // altrimenti non ci si potrebbe mai autenticare.
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        // La bacheca si puo' leggere senza account.
                        .requestMatchers(HttpMethod.GET, "/api/posts", "/api/posts/**").permitAll()
                        // I file caricati sono serviti staticamente.
                        .requestMatchers("/files/**").permitAll()
                        // Il geocoding serve mentre si compila il form.
                        .requestMatchers("/api/geo/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        // Tutto il resto richiede un token valido.
                        .anyRequest().authenticated())
                /*
                  Senza questo, a una richiesta non autenticata Spring
                  risponderebbe 403 Forbidden. Per una API REST e'
                  sbagliato: 403 significa "so chi sei ma non puoi",
                  mentre qui il caso e' "non so chi sei", cioe' 401.
                  Il frontend usa proprio il 401 per capire quando
                  mandare l'utente alla pagina di accesso.
                */
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            response.getWriter().write("""
                                    {"status":401,"message":"Devi accedere per compiere questa operazione","details":[]}""");
                        }))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
