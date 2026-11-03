package br.com.raizes.service;

import br.com.raizes.dto.PagamentoRequest;
import br.com.raizes.dto.PagamentoResponse;
import br.com.raizes.exception.ApiException;
import br.com.raizes.model.Pagamento;
import br.com.raizes.model.Pedido;
import br.com.raizes.model.StatusPagamento;
import br.com.raizes.model.StatusPedido;
import br.com.raizes.repository.PagamentoRepository;
import br.com.raizes.repository.PedidoRepository;
import br.com.raizes.security.UsuarioLogado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pagamento do pedido (fluxo: pedido -> pagamento mock -> status do pedido).
 * Nós NÃO cobramos nada: pedimos ao "serviço externo" (mock), registramos a resposta e atualizamos o pedido.
 */
@Service
public class PagamentoService {

    private static final Logger log = LoggerFactory.getLogger(PagamentoService.class);

    private final PedidoRepository pedidoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final PagamentoMock pagamentoMock;

    public PagamentoService(PedidoRepository pedidoRepository, PagamentoRepository pagamentoRepository,
                            PagamentoMock pagamentoMock) {
        this.pedidoRepository = pedidoRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.pagamentoMock = pagamentoMock;
    }

    @Transactional
    public PagamentoResponse pagar(Long pedidoId, PagamentoRequest request, UsuarioLogado usuario) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PEDIDO_NAO_ENCONTRADO", "Pedido não encontrado."));

        // Só o dono do pedido paga
        if (!pedido.getClienteId().equals(usuario.id())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACESSO_NEGADO", "Você não tem permissão para pagar este pedido.");
        }
        // Só paga se ainda estiver aguardando pagamento (ou se o pagamento anterior foi recusado)
        if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO && pedido.getStatus() != StatusPedido.PAGAMENTO_RECUSADO) {
            throw new ApiException(HttpStatus.CONFLICT, "PEDIDO_NAO_PAGAVEL",
                    "O pedido está com status " + pedido.getStatus() + " e não aceita pagamento.");
        }

        // 1) Pede o pagamento ao serviço externo (mock)
        PagamentoMock.Resultado resultado = pagamentoMock.processar(request.simularResultado());

        // 2) Registra o resultado
        Pagamento pagamento = pagamentoRepository.save(new Pagamento(
                pedido.getId(), request.formaPagamento(), resultado.status(), pedido.getTotal(), resultado.referencia()));

        // 3) Atualiza o status do pedido
        if (resultado.status() == StatusPagamento.APROVADO) {
            pedido.setStatus(StatusPedido.PAGO);
        } else {
            pedido.setStatus(StatusPedido.PAGAMENTO_RECUSADO); // o estoque continua reservado para uma nova tentativa
        }

        log.info("LOG acao=PAGAMENTO_{} usuarioId={} pedidoId={}", resultado.status(), usuario.id(), pedido.getId());
        return new PagamentoResponse(pagamento.getId(), pedido.getId(), resultado.status(), pedido.getTotal(),
                resultado.referencia(), resultado.mensagem(), pedido.getStatus());
    }
}
