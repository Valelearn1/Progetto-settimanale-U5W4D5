package com.example.demo.config;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/*
  Crea un utente di prova al primo avvio, cosi' si puo' accedere
  subito senza doversi prima registrare. La password e' salvata con lo
  stesso hash BCrypt usato dalla registrazione: e' un utente normale a
  tutti gli effetti.

  Credenziali: demo / demo1234
*/
@Component
public class DataSeeder {

    public static final String DEMO_USERNAME = "demo";
    private static final String DEMO_PASSWORD = "demo1234";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedDemoUser() {
        if (userRepository.existsByUsername(DEMO_USERNAME)) {
            return;
        }
        User demoUser = User.builder()
                .nomeCompleto("Utente Demo")
                .username(DEMO_USERNAME)
                .email("demo@example.com")
                .password(passwordEncoder.encode(DEMO_PASSWORD))
                .build();
        userRepository.save(demoUser);
    }
}
