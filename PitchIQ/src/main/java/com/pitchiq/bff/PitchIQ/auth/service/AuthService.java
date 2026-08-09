package com.pitchiq.bff.PitchIQ.auth.service;

import com.pitchiq.bff.PitchIQ.auth.dto.LoginRequest;
import com.pitchiq.bff.PitchIQ.auth.dto.LoginResponse;
import com.pitchiq.bff.PitchIQ.auth.repository.UtenteRepository;
import com.pitchiq.bff.PitchIQ.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest req) {
        // Non distinguiamo "utente non esiste" da "password sbagliata":
        // un messaggio generico evita user enumeration attack.
        var utente = utenteRepository.findByUsername(req.username())
                .orElseThrow(() -> new BadCredentialsException("Credenziali non valide"));

        if (!passwordEncoder.matches(req.password(), utente.getPasswordHash())) {
            throw new BadCredentialsException("Credenziali non valide");
        }

        String token = jwtService.generateToken(utente.getUsername(), utente.getRuolo());

        return new LoginResponse(token, jwtService.getExpirationSeconds(), utente.getRuolo());
    }
}
