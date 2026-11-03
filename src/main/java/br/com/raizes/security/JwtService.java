package br.com.raizes.security;

import br.com.raizes.exception.ApiException;
import br.com.raizes.model.Perfil;
import br.com.raizes.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Cria e confere o token JWT.
 * O token é um texto assinado que guarda o id e o perfil do usuário. Quem não tem um token válido não entra.
 */
@Service
public class JwtService {

    private final SecretKey chave;
    private final long expiracaoSegundos;

    public JwtService(@Value("${app.jwt.secret}") String segredo,
                      @Value("${app.jwt.expiration-minutes}") long expiracaoMinutos) {
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.expiracaoSegundos = expiracaoMinutos * 60;
    }

    /** Cria o token no login. */
    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .claim("perfil", usuario.getPerfil().name())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusSeconds(expiracaoSegundos)))
                .signWith(chave)
                .compact();
    }

    public long getExpiracaoSegundos() {
        return expiracaoSegundos;
    }

    /**
     * Usado nas rotas protegidas: recebe o cabeçalho "Authorization: Bearer <token>",
     * confere o token e devolve quem é o usuário. Se estiver ausente ou inválido, responde 401.
     */
    public UsuarioLogado autenticar(String cabecalhoAuthorization) {
        if (cabecalhoAuthorization == null || !cabecalhoAuthorization.startsWith("Bearer ")) {
            throw naoAutenticado();
        }
        try {
            Claims claims = Jwts.parser().verifyWith(chave).build()
                    .parseSignedClaims(cabecalhoAuthorization.substring(7)).getPayload();
            return new UsuarioLogado(Long.valueOf(claims.getSubject()),
                    Perfil.valueOf(claims.get("perfil", String.class)));
        } catch (RuntimeException e) {
            throw naoAutenticado();
        }
    }

    private ApiException naoAutenticado() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "NAO_AUTENTICADO", "Token de acesso ausente, inválido ou expirado.");
    }
}
