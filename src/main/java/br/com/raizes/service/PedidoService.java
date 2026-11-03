package br.com.raizes.service;

import br.com.raizes.dto.Pagina;
import br.com.raizes.dto.PedidoRequest;
import br.com.raizes.exception.ApiException;
import br.com.raizes.exception.ErroResponse;
import br.com.raizes.model.CanalPedido;
import br.com.raizes.model.ItemPedido;
import br.com.raizes.model.Pedido;
import br.com.raizes.model.Perfil;
import br.com.raizes.model.Produto;
import br.com.raizes.model.StatusPedido;
import br.com.raizes.repository.PedidoRepository;
import br.com.raizes.repository.ProdutoRepository;
import br.com.raizes.repository.UnidadeRepository;
import br.com.raizes.security.UsuarioLogado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/** Regras dos pedidos: criar (com controle de estoque), consultar, listar e mudar o status. */
@Service
public class PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

    private final PedidoRepository pedidoRepository;
    private final UnidadeRepository unidadeRepository;
    private final ProdutoRepository produtoRepository;

    public PedidoService(PedidoRepository pedidoRepository, UnidadeRepository unidadeRepository,
                         ProdutoRepository produtoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.unidadeRepository = unidadeRepository;
        this.produtoRepository = produtoRepository;
    }

    /**
     * @Transactional = "tudo ou nada": se der erro no meio (ex.: falta de estoque),
     * o banco desfaz tudo o que já tinha sido feito nesta operação.
     */
    @Transactional
    public Pedido criar(PedidoRequest request, UsuarioLogado usuario) {
        // 1) A unidade existe?
        if (!unidadeRepository.existsById(request.unidadeId())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "UNIDADE_NAO_ENCONTRADA", "Unidade não encontrada.");
        }

        Pedido pedido = new Pedido(usuario.id(), request.unidadeId(), request.canalPedido(), request.formaPagamento());
        BigDecimal total = BigDecimal.ZERO;

        // 2) Para cada item do pedido
        for (int i = 0; i < request.itens().size(); i++) {
            PedidoRequest.Item item = request.itens().get(i);
            int quantidade = item.quantidade();

            // O produto existe e pertence a esta unidade?
            Produto produto = produtoRepository.findById(item.produtoId()).orElse(null);
            if (produto == null || !produto.getUnidadeId().equals(request.unidadeId())) {
                throw new ApiException(HttpStatus.NOT_FOUND, "PRODUTO_NAO_ENCONTRADO",
                        "O produto " + item.produtoId() + " não existe nesta unidade.");
            }
            // Tem estoque suficiente?
            if (produto.getQuantidade() < quantidade) {
                throw new ApiException(HttpStatus.CONFLICT, "ESTOQUE_INSUFICIENTE",
                        "Não há quantidade suficiente para um ou mais itens.",
                        List.of(new ErroResponse.Detalhe("itens[" + i + "].quantidade", "Disponível: " + produto.getQuantidade())));
            }
            // Tira do estoque e coloca o item no pedido
            produto.baixarEstoque(quantidade);
            pedido.adicionarItem(new ItemPedido(produto.getId(), quantidade, produto.getPreco()));
            total = total.add(produto.getPreco().multiply(BigDecimal.valueOf(quantidade)));
        }

        // 3) Grava o pedido com o total calculado
        pedido.setTotal(total);
        pedidoRepository.save(pedido);
        log.info("LOG acao=PEDIDO_CRIADO usuarioId={} pedidoId={} canalPedido={} total={}",
                usuario.id(), pedido.getId(), pedido.getCanalPedido(), total);
        return pedido;
    }

    public Pedido buscar(Long id, UsuarioLogado usuario) {
        Pedido pedido = buscarOuErro(id);
        // Cliente só pode ver os próprios pedidos
        if (usuario.perfil() == Perfil.CLIENTE && !pedido.getClienteId().equals(usuario.id())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACESSO_NEGADO", "Você não tem permissão para ver este pedido.");
        }
        return pedido;
    }

    public Pagina<Pedido> listar(UsuarioLogado usuario, CanalPedido canalPedido, int page, int limit) {
        if (page < 1 || limit < 1 || limit > 100) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "PAGINACAO_INVALIDA",
                    "page deve ser maior que 0 e limit deve estar entre 1 e 100.");
        }
        Pageable pagina = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "id"));

        Page<Pedido> resultado;
        if (usuario.perfil() == Perfil.CLIENTE) {
            // Cliente vê só os dele
            resultado = (canalPedido == null)
                    ? pedidoRepository.findByClienteId(usuario.id(), pagina)
                    : pedidoRepository.findByClienteIdAndCanalPedido(usuario.id(), canalPedido, pagina);
        } else {
            // Gerente vê todos
            resultado = (canalPedido == null)
                    ? pedidoRepository.findAll(pagina)
                    : pedidoRepository.findByCanalPedido(canalPedido, pagina);
        }
        return new Pagina<>(resultado.getContent(), page, limit, resultado.getTotalElements());
    }

    @Transactional
    public Pedido atualizarStatus(Long id, StatusPedido novoStatus, UsuarioLogado usuario) {
        Pedido pedido = buscarOuErro(id);
        StatusPedido anterior = pedido.getStatus();

        if (!anterior.podeIrPara(novoStatus)) {
            throw new ApiException(HttpStatus.CONFLICT, "TRANSICAO_INVALIDA",
                    "Não é possível mudar o pedido de " + anterior + " para " + novoStatus + ".");
        }
        pedido.setStatus(novoStatus);

        // Pedido cancelado: os itens voltam para o estoque
        if (novoStatus == StatusPedido.CANCELADO) {
            for (ItemPedido item : pedido.getItens()) {
                produtoRepository.findById(item.getProdutoId()).ifPresent(p -> p.devolverEstoque(item.getQuantidade()));
            }
        }
        log.info("LOG acao=STATUS_ALTERADO usuarioId={} pedidoId={} de={} para={}", usuario.id(), id, anterior, novoStatus);
        return pedido;
    }

    private Pedido buscarOuErro(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PEDIDO_NAO_ENCONTRADO", "Pedido não encontrado."));
    }
}
