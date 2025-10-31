package br.com.aeris.aeris_autentification.controller;

import br.com.aeris.aeris_autentification.dto.LoginColaboradorResponse;
import br.com.aeris.aeris_autentification.dto.LoginRequest;
import br.com.aeris.aeris_autentification.dto.LoginResponse;
import br.com.aeris.aeris_autentification.service.AuthService;
import br.com.aeris.aeris_autentification.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new LoginResponse(null, null, null, e.getMessage()));
        }
    }

    @GetMapping("/validate")
    @Operation(summary = "Validar token", description = "Valida se um token JWT é válido")
    public ResponseEntity<Map<String, Object>> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(token);
            boolean isValid = jwtUtil.validateToken(token, email);

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
    public ResponseEntity<LoginColaboradorResponse> loginColaborador(@RequestBody LoginRequest request) {
        try {
            LoginColaboradorResponse response = authService.loginColaborador(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new LoginColaboradorResponse(null, null, null, null, e.getMessage()));
        }
    }
}
