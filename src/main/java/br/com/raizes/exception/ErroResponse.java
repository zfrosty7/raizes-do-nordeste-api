package br.com.raizes.exception;

import java.time.Instant;
import java.util.List;

/** Formato de erro ÚNICO da API: toda falha devolve este mesmo JSON. */
public record ErroResponse(String error, String message, List<Detalhe> details, String timestamp, String path) {

    /** Qual campo deu problema e qual foi o problema. */
    public record Detalhe(String field, String issue) {
    }

    public static ErroResponse de(String error, String message, List<Detalhe> details, String path) {
        return new ErroResponse(error, message, details, Instant.now().toString(), path);
    }
}
