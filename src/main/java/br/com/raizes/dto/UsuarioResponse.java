package br.com.raizes.dto;

import br.com.raizes.model.Perfil;

/** O que mostramos de um usuário: nunca a senha nem o hash. */
public record UsuarioResponse(Long id, String nome, Perfil perfil) {
}
