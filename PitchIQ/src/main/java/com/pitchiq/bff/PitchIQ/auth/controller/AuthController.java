package com.pitchiq.bff.PitchIQ.auth.controller;

import com.pitchiq.bff.PitchIQ.auth.dto.LoginRequest;
import com.pitchiq.bff.PitchIQ.auth.dto.LoginResponse;
import com.pitchiq.bff.PitchIQ.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    // Gestione locale di BadCredentials: restituisce 401 invece del 500
    // di default, così il frontend distingue "credenziali errate" da errori reali.
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, ex.getMessage());
        pd.setTitle("Autenticazione fallita");
        return pd;
    }
}
