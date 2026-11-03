package br.com.raizes.service;

import br.com.raizes.model.StatusPagamento;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * PAGAMENTO FALSO (mock): finge ser um serviço externo de pagamento.
 * Não cobra nada de ninguém. Aprova, a não ser que o teste peça para recusar.
 */
@Component
public class PagamentoMock {

    /** O que o "serviço externo" responde. */
    public record Resultado(StatusPagamento status, String referencia, String mensagem) {
    }

    public Resultado processar(StatusPagamento simularResultado) {
        StatusPagamento status = (simularResultado != null) ? simularResultado : StatusPagamento.APROVADO;
        String mensagem = (status == StatusPagamento.APROVADO) ? "Pagamento aprovado (simulado)." : "Pagamento recusado (simulado).";
        return new Resultado(status, "MOCK-" + UUID.randomUUID(), mensagem);
    }
}
