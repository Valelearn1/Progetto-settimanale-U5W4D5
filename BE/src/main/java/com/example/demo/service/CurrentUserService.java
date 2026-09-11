package com.example.demo.service;

import com.example.demo.config.DataSeeder;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

// Unico punto del codice che sa "chi e' l'utente che sta operando".
// Oggi restituisce sempre l'utente finto creato da DataSeeder; in
// fase 9, quando arriva JWT, questo e' l'unico metodo da riscrivere
// (leggera' l'utente dal token invece che dal seed) senza toccare
// nessun altro service.
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        return userRepository.findByUsername(DataSeeder.DEMO_USERNAME)
                .orElseThrow(() -> new ResourceNotFoundException("Utente demo non trovato"));
    }
}
