package br.com.raizes.security;

import br.com.raizes.exception.ApiException;
import br.com.raizes.model.Perfil;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

/** Quem está fazendo a requisição (descoberto pelo token). */
public record UsuarioLogado(Long id, Perfil perfil) {

    /** Autorização por perfil: se o perfil não estiver na lista, responde 403. */
    public void exigirPerfil(Perfil... permitidos) {
        if (!Arrays.asList(permitidos).contains(perfil)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACESSO_NEGADO", "Seu perfil não tem permissão para esta ação.");
        }
    }
}
