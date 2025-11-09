package br.com.aeris.aeris_autentification.service;

import br.com.aeris.aeris_autentification.dto.LoginColaboradorResponse;
import br.com.aeris.aeris_autentification.dto.LoginRequest;
import br.com.aeris.aeris_autentification.dto.LoginResponse;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public LoginResponse login(LoginRequest request) {
        logger.info("[AuthService.login] Iniciando login para o email: {}", request.getEmail());

        // Buscar usuário pelo email
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    logger.warn("[AuthService.login] Usuário não encontrado para o email: {}", request.getEmail());
                    return new EntityNotFoundException("Credenciais inválidas");
                });

        logger.info("[AuthService.login] Usuário encontrado: ID={}, Nome={}, Tipo={}",
                usuario.getId(), usuario.getNome(), usuario.getTipo());

        // Verificar se o usuário está ativo
        if (!usuario.getAtivo()) {
            logger.warn("[AuthService.login] Tentativa de login com usuário inativo: ID={}", usuario.getId());
            throw new IllegalArgumentException("Usuário inativo");
        }

        // Verificar se o usuário é administrador
        if (!Objects.equals(usuario.getTipo(), "adm")) {
            logger.warn("[AuthService.login] Acesso negado: usuário ID={} não é administrador", usuario.getId());
            throw new IllegalArgumentException("Usuário não tem acesso");
        }

        // Verificar senha com BCrypt
        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            logger.warn("[AuthService.login] Senha incorreta para o usuário ID={}", usuario.getId());
            throw new IllegalArgumentException("Credenciais inválidas");
        }

        logger.info("[AuthService.login] Credenciais validadas com sucesso para usuário ID={}", usuario.getId());

        // Gerar token JWT
        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getNome(), usuario.getTipo());
        logger.info("[AuthService.login] Token JWT gerado para usuário ID={}", usuario.getId());

        logger.info("[AuthService.login] Login finalizado com sucesso para usuário ID={}", usuario.getId());

        return new LoginResponse(
                token,
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getEmpresa().getId(),
                "Login realizado com sucesso"
        );
    }

    public LoginColaboradorResponse loginColaborador(LoginRequest request) {
        logger.info("[AuthService.loginColaborador] Iniciando login de colaborador para o email: {}", request.getEmail());

        // Buscar usuário pelo email
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    logger.warn("[AuthService.loginColaborador] Usuário não encontrado para o email: {}", request.getEmail());
                    return new EntityNotFoundException("Credenciais inválidas");
                });

        logger.info("[AuthService.loginColaborador] Usuário encontrado: ID={}, Nome={}, Tipo={}",
                usuario.getId(), usuario.getNome(), usuario.getTipo());

        // Verificar se o usuário é colaborador
        if (!Objects.equals(usuario.getTipo(), "colaborador")) {
            logger.warn("[AuthService.loginColaborador] Acesso negado: usuário ID={} não é colaborador", usuario.getId());
            throw new IllegalArgumentException("Usuário não tem acesso");
        }

        // Buscar pesquisas do colaborador
        logger.info("[AuthService.loginColaborador] Buscando pesquisas associadas ao colaborador ID={}", usuario.getId());
        List<PesquisaColaborador> pesquisasColaborador = pesquisaColaboradorRepository.findByUsuario(usuario);

        List<PesquisaColaborador> pesquisasFiltradas = new ArrayList<>();

        for (PesquisaColaborador pesquisa : pesquisasColaborador) {
            if (pesquisaRepository.existsById(pesquisa.getPesquisa().getId()) && pesquisa.getPesquisa().getAtivo()) {
                pesquisasFiltradas.add(pesquisa);
                logger.info("[AuthService.loginColaborador] Pesquisa ativa encontrada: ID={}", pesquisa.getPesquisa().getId());
            }
        }

        if (pesquisasFiltradas.isEmpty()) {
            logger.warn("[AuthService.loginColaborador] Nenhuma pesquisa ativa encontrada para o colaborador ID={}", usuario.getId());
            throw new RuntimeException("Usuário não possui pesquisas");
        }

        PesquisaColaborador pesquisaEncontrada = pesquisasFiltradas.stream()
                .filter(p -> passwordEncoder.matches(request.getSenha(), p.getToken()) && !p.isRespondido())
                .findFirst()
                .orElse(null);

        if (pesquisaEncontrada == null) {
            logger.warn("[AuthService.loginColaborador] Nenhuma pesquisa ativa corresponde ao token informado pelo usuário ID={}", usuario.getId());
            throw new EntityNotFoundException("Token não corresponde a nenhuma pesquisa ativa");
        }

        logger.info("[AuthService.loginColaborador] Pesquisa válida encontrada: ID={}, para colaborador ID={}",
                pesquisaEncontrada.getPesquisa().getId(), usuario.getId());

        // Gerar token JWT
        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getNome(), usuario.getTipo());
        logger.info("[AuthService.loginColaborador] Token JWT gerado para colaborador ID={}", usuario.getId());

        logger.info("[AuthService.loginColaborador] Login de colaborador finalizado com sucesso: ID={}, Pesquisa ID={}",
                usuario.getId(), pesquisaEncontrada.getPesquisa().getId());

        return LoginColaboradorResponse.builder()
                .token(token)
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .idPesquisa(pesquisaEncontrada.getPesquisa().getId())
                .mensagem("Login realizado com sucesso")
                .build();
    }

    public Boolean isAdm(String token) {
        logger.info("[AuthService.isAdm] Verificando tipo de usuário a partir do token JWT");
        boolean isAdmin = Objects.equals(jwtUtil.extractTipo(token), "adm");
        logger.info("[AuthService.isAdm] Resultado da verificação: {}", isAdmin ? "Administrador" : "Não administrador");
        return isAdmin;
    }
}
