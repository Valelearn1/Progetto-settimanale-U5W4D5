package com.example.demo.service;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.dto.response.AuthResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username gia' in uso: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email gia' registrata: " + request.email());
        }

        User user = User.builder()
                .nomeCompleto(request.nomeCompleto())
                .username(request.username())
                .email(request.email())
                // Mai la password in chiaro: si salva solo l'hash, e da
                // quello non si puo' risalire alla password originale.
                .password(passwordEncoder.encode(request.password()))
                .build();

        return buildResponse(userRepository.saveAndFlush(user));
    }

    public AuthResponse login(LoginRequest request) {
        /*
          Stesso messaggio di errore sia se lo username non esiste sia
          se la password e' sbagliata: distinguerli direbbe a un
          malintenzionato quali username sono registrati.
        */
        User user = userRepository.findByUsername(request.username())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
                .orElseThrow(() -> new UnauthorizedException("Username o password non corretti"));

        return buildResponse(user);
    }

    private AuthResponse buildResponse(User user) {
        return new AuthResponse(
                jwtService.generateToken(user.getUsername()),
                jwtService.getExpirationMillis(),
                UserResponse.from(user)
        );
    }
}
