package br.com.aeris.aeris_autentification.service;

import br.com.aeris.aeris_autentification.dto.LoginColaboradorResponse;
import br.com.aeris.aeris_autentification.dto.LoginRequest;
import br.com.aeris.aeris_autentification.dto.LoginResponse;
import br.com.aeris.aeris_autentification.model.Pesquisa;
import br.com.aeris.aeris_autentification.model.PesquisaColaborador;
import br.com.aeris.aeris_autentification.model.Usuario;
import br.com.aeris.aeris_autentification.repository.PesquisaColaboradorRepository;
import br.com.aeris.aeris_autentification.repository.PesquisaRepository;
import br.com.aeris.aeris_autentification.repository.UsuarioRepository;
import br.com.aeris.aeris_autentification.util.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class AuthService {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PesquisaColaboradorRepository pesquisaColaboradorRepository;

    @Autowired
    private PesquisaRepository pesquisaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        // Buscar usuário pelo email
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Credenciais inválidas"));

        // Verificar se o usuário está ativo
        if (!usuario.getAtivo()) {
            throw new IllegalArgumentException("Usuário inativo");
        }

        // Verificar se o usuário é admistrador
        if (!Objects.equals(usuario.getTipo(), "adm")) {
            throw new IllegalArgumentException("Usuário não tem acesso");
        }

        // Verificar senha com BCrypt
        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }

        // Gerar token JWT
        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getNome(), usuario.getTipo());

        return new LoginResponse(
                token,
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getEmpresa().getId(),
                "Login realizado com sucesso"
        );
    }

    public LoginColaboradorResponse loginColaborador(LoginRequest request) {
        // Buscar usuário pelo email
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Credenciais inválidas"));

        // Verificar se o usuário é colaborador
        if (!Objects.equals(usuario.getTipo(), "colaborador")) {
            throw new IllegalArgumentException("Usuário não tem acesso");
        }

        // Buscar pesquisas do colaborador pelo id
        List<PesquisaColaborador> pesquisasColaborador = pesquisaColaboradorRepository.findByUsuario(usuario);

        List<PesquisaColaborador> pesquisasFiltradas = new ArrayList<>();

        for(PesquisaColaborador pesquisa: pesquisasColaborador){
            if(pesquisaRepository.existsById(pesquisa.getPesquisa().getId())){
                if(pesquisa.getPesquisa().getAtivo()){
                    pesquisasFiltradas.add(pesquisa);
                }
            }
        }

        if (pesquisasFiltradas.isEmpty()) {
            throw new RuntimeException("Usuário não possui pesquisas");
        }

        PesquisaColaborador pesquisaEncontrada = pesquisasFiltradas.stream()
                .filter(p -> passwordEncoder.matches(request.getSenha(), p.getToken()) && !p.isRespondido())
                .findFirst()
                .orElse(null);

        if(pesquisaEncontrada == null){
            throw new EntityNotFoundException("Token não corresponde a nenhuma pesquisa ativa");
        }

        // Gerar token JWT
        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getNome(), usuario.getTipo());



        return LoginColaboradorResponse.builder()
                .token(token)
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .idPesquisa(pesquisaEncontrada.getPesquisa().getId())
                .mensagem("Login realizado com sucesso")
                .build();
    }

    public Boolean isAdm(String token){
        return Objects.equals(jwtUtil.extractTipo(token), "adm");
    }
}
