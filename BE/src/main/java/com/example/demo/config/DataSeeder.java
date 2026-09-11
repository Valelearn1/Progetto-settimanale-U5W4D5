package com.example.demo.config;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// Crea un utente "finto" al primo avvio: le fasi 5, 6 e 7 lavorano
// su questo utente perche' registrazione/login/JWT arrivano solo
// nelle fasi 8 e 9 (vedi PROGETTAZIONE.txt). CurrentUserService legge
// sempre questo utente finche' l'autenticazione non esiste.
@Component
public class DataSeeder {

    public static final String DEMO_USERNAME = "demo";

    private final UserRepository userRepository;

    public DataSeeder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedDemoUser() {
        if (userRepository.findByUsername(DEMO_USERNAME).isPresent()) {
            return;
        }
        User demoUser = User.builder()
                .nomeCompleto("Utente Demo")
                .username(DEMO_USERNAME)
                .email("demo@example.com")
                // Placeholder: niente hash reale finche' non c'e' la
                // registrazione (fase 8). Nessun login possibile con
                // questo utente prima di allora.
                .password("N/A - nessun login prima della fase 8")
                .build();
        userRepository.save(demoUser);
    }
}
