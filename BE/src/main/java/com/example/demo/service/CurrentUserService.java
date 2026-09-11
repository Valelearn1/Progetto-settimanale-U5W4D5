package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/*
  Unico punto del codice che sa "chi e' l'utente che sta operando".

  Fino alla fase 7 restituiva un utente finto creato all'avvio; ora
  legge l'utente dal token JWT, messo nel contesto di sicurezza da
  JwtAuthFilter. Nessun altro service e' stato toccato: era
  esattamente lo scopo di tenere questo metodo in un posto solo.
*/
@Service
public class CurrentUserService {

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new UnauthorizedException("Devi accedere per compiere questa operazione");
        }
        return user;
    }
}
