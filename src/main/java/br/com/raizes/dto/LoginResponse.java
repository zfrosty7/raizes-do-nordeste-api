package br.com.raizes.dto;

public record LoginResponse(String accessToken, String tokenType, long expiresIn, UsuarioResponse user) {
}
