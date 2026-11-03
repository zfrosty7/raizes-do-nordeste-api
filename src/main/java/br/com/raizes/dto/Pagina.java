package br.com.raizes.dto;

import java.util.List;

/** Resposta das listagens: os itens da página e o total. */
public record Pagina<T>(List<T> itens, int page, int limit, long total) {
}
