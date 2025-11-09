package br.com.aeris.aeris_autentification.controller;

import br.com.aeris.aeris_autentification.dto.ErrorResponse;
import br.com.aeris.aeris_autentification.dto.LoginColaboradorResponse;
import br.com.aeris.aeris_autentification.dto.LoginRequest;
import br.com.aeris.aeris_autentification.dto.LoginResponse;
import br.com.aeris.aeris_autentification.service.AuthService;
import br.com.aeris.aeris_autentification.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Endpoints para autenticação de usuários")
public class AuthController {
    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    @Operation(summary = "Realizar login", description = "Autentica um usuario e retorna um token JWT")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
        // Erro de validação (400)
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.badRequest().body(error);

    } catch (EntityNotFoundException e) {
        // Não encontrado (404)
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

    } catch (Exception e) {
        // Erro genérico (500)
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Erro interno no servidor")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

}

    @GetMapping("/validate")
    @Operation(summary = "Validar token", description = "Valida se um token JWT é válido")
    public ResponseEntity<Map<String, Object>> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(token);
            boolean isValid = jwtUtil.validateToken(token);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("email", email);
            response.put("expiration", jwtUtil.extractExpiration(token));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", "Token inválido");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login-colaborador")
    @Operation(summary = "Realizar login do usuário colaborador", description = "Autentica um usuario colaborador e retorna um token JWT")
    public ResponseEntity<?> loginColaborador(@RequestBody LoginRequest request) {
        try {
            LoginColaboradorResponse response = authService.loginColaborador(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // Erro de validação (400)
            ErrorResponse error = ErrorResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())

                    .build();
            return ResponseEntity.badRequest().body(error);

        } catch (EntityNotFoundException e) {
            // Não encontrado (404)
            ErrorResponse error = ErrorResponse.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (Exception e) {
            // Erro genérico (500)
            ErrorResponse error = ErrorResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Erro interno no servidor")
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/isAdm")
    public ResponseEntity<Boolean> isAdm() {
        // O token já foi validado pelo filtro JWT
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.ok(false);
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        return ResponseEntity.ok(isAdmin);
    }
}
