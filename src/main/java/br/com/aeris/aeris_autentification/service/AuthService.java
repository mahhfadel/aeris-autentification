package br.com.aeris.aeris_autentification.service;

import br.com.aeris.aeris_autentification.dto.LoginRequest;
import br.com.aeris.aeris_autentification.dto.LoginResponse;
import br.com.aeris.aeris_autentification.model.Usuario;
import br.com.aeris.aeris_autentification.repository.UsuarioRepository;
import br.com.aeris.aeris_autentification.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AuthService {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        // Buscar usuário pelo email
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciais inválidas"));

        // Verificar se o usuário está ativo
        if (!usuario.getAtivo()) {
            throw new RuntimeException("Usuário inativo");
        }

        // Verificar se o usuário é admistrador
        if (!Objects.equals(usuario.getTipo(), "adm")) {
            throw new RuntimeException("Usuário não tem acesso");
        }

        // Verificar senha com BCrypt
        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            throw new RuntimeException("Credenciais inválidas");
        }

        // Gerar token JWT
        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getNome());

        return new LoginResponse(
                token,
                usuario.getNome(),
                usuario.getEmail(),
                "Login realizado com sucesso"
        );
    }
}
