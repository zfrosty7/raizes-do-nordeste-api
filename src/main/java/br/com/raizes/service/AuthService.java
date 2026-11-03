package br.com.raizes.service;

import br.com.raizes.dto.LoginRequest;
import br.com.raizes.dto.LoginResponse;
import br.com.raizes.dto.RegistroRequest;
import br.com.raizes.dto.UsuarioResponse;
import br.com.raizes.exception.ApiException;
import br.com.raizes.exception.ErroResponse;
import br.com.raizes.model.Perfil;
import br.com.raizes.model.Usuario;
import br.com.raizes.repository.UsuarioRepository;
import br.com.raizes.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/** Regras de cadastro e login. */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(); // gera e confere o hash da senha

    public AuthService(UsuarioRepository usuarioRepository, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    public UsuarioResponse registrar(RegistroRequest request) {
        // LGPD: sem consentimento, não cadastra
        if (!request.consentimentoLgpd()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "CONSENTIMENTO_OBRIGATORIO",
                    "É necessário consentir com o uso dos dados pessoais (LGPD) para se cadastrar.",
                    List.of(new ErroResponse.Detalhe("consentimentoLgpd", "Deve ser true.")));
        }
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_JA_CADASTRADO", "Já existe um usuário com este e-mail.");
        }
        // Quem se cadastra pela API sempre vira CLIENTE (o perfil não vem do JSON)
        Usuario usuario = usuarioRepository.save(
                new Usuario(request.nome().trim(), email, encoder.encode(request.senha()), Perfil.CLIENTE, true));
        log.info("LOG acao=USUARIO_CADASTRADO usuarioId={}", usuario.getId());
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getPerfil());
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> credenciaisInvalidas());
        if (!encoder.matches(request.senha(), usuario.getSenhaHash())) {
            throw credenciaisInvalidas();
        }
        UsuarioResponse resumo = new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getPerfil());
        return new LoginResponse(jwtService.gerarToken(usuario), "Bearer", jwtService.getExpiracaoSegundos(), resumo);
    }

    private ApiException credenciaisInvalidas() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "CREDENCIAIS_INVALIDAS", "E-mail ou senha inválidos.");
    }
}
