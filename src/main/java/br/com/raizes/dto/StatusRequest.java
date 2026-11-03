package br.com.raizes.dto;

import br.com.raizes.model.StatusPedido;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(
        @NotNull(message = "O status é obrigatório.")
        StatusPedido status) {
}
