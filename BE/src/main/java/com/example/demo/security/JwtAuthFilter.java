package com.example.demo.security;

import com.example.demo.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/*
  Intercetta ogni richiesta, cerca il token nell'intestazione
  Authorization e, se e' valido, dice a Spring Security chi sta
  chiamando.

  Estende OncePerRequestFilter perche' una stessa richiesta puo'
  attraversare la catena dei filtri piu' volte (per esempio con un
  forward interno): la classe base garantisce un controllo solo.

  Se il token manca o non e' valido il filtro non blocca nulla: lascia
  la richiesta senza utente autenticato, e sara' la configurazione di
  sicurezza a decidere se quella rotta era pubblica o meno.
*/
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(HEADER);
        if (header != null && header.startsWith(PREFIX)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String token = header.substring(PREFIX.length());
            jwtService.extractUsername(token)
                    .flatMap(userRepository::findByUsername)
                    .ifPresent(user -> {
                        // L'utente diventa il "principal": da qui lo
                        // legge CurrentUserService.
                        var authentication = new UsernamePasswordAuthenticationToken(
                                user, null, List.of());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    });
        }

        filterChain.doFilter(request, response);
    }
}
