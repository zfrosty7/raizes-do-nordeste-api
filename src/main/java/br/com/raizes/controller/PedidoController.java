package br.com.raizes.controller;

import br.com.raizes.dto.PagamentoRequest;
import br.com.raizes.dto.PagamentoResponse;
import br.com.raizes.dto.Pagina;
import br.com.raizes.dto.PedidoRequest;
import br.com.raizes.dto.StatusRequest;
import br.com.raizes.model.CanalPedido;
import br.com.raizes.model.Pedido;
import br.com.raizes.model.Perfil;
import br.com.raizes.security.JwtService;
import br.com.raizes.security.UsuarioLogado;
import br.com.raizes.service.PagamentoService;
import br.com.raizes.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rotas dos pedidos. Todas seguem o mesmo padrão:
 *   1) jwtService.autenticar(...)  -> confere o token (401 se inválido) e descobre quem é o usuário
 *   2) usuario.exigirPerfil(...)   -> confere se o perfil pode usar a rota (403 se não puder)
 *   3) chama o service que faz o trabalho
 */
@RestController
@RequestMapping("/pedidos")
@Tag(name = "Pedidos", description = "Pedidos, status e pagamento (mock)")
@SecurityRequirement(name = "bearerAuth")
public class PedidoController {

    private final PedidoService pedidoService;
    private final PagamentoService pagamentoService;
    private final JwtService jwtService;

    public PedidoController(PedidoService pedidoService, PagamentoService pagamentoService, JwtService jwtService) {
        this.pedidoService = pedidoService;
        this.pagamentoService = pagamentoService;
        this.jwtService = jwtService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um pedido (somente CLIENTE). O canalPedido é obrigatório")
    public Pedido criar(@RequestHeader(value = "Authorization", required = false) String authorization,
                        @Valid @RequestBody PedidoRequest request) {
        UsuarioLogado usuario = jwtService.autenticar(authorization);
        usuario.exigirPerfil(Perfil.CLIENTE);
        return pedidoService.criar(request, usuario);
    }

    @GetMapping
    @Operation(summary = "Lista pedidos. Filtro opcional: canalPedido. Paginação: page e limit")
    public Pagina<Pedido> listar(@RequestHeader(value = "Authorization", required = false) String authorization,
                                 @RequestParam(required = false) CanalPedido canalPedido,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int limit) {
        UsuarioLogado usuario = jwtService.autenticar(authorization);
        return pedidoService.listar(usuario, canalPedido, page, limit);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta um pedido pelo id")
    public Pedido buscar(@RequestHeader(value = "Authorization", required = false) String authorization,
                         @PathVariable Long id) {
        UsuarioLogado usuario = jwtService.autenticar(authorization);
        return pedidoService.buscar(id, usuario);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualiza o status do pedido (somente GERENTE)")
    public Pedido atualizarStatus(@RequestHeader(value = "Authorization", required = false) String authorization,
                                  @PathVariable Long id,
                                  @Valid @RequestBody StatusRequest request) {
        UsuarioLogado usuario = jwtService.autenticar(authorization);
        usuario.exigirPerfil(Perfil.GERENTE);
        return pedidoService.atualizarStatus(id, request.status(), usuario);
    }

    @PostMapping("/{id}/pagamentos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Pagamento mock (somente CLIENTE). Use simularResultado=RECUSADO para simular falha")
    public PagamentoResponse pagar(@RequestHeader(value = "Authorization", required = false) String authorization,
                                   @PathVariable Long id,
                                   @Valid @RequestBody PagamentoRequest request) {
        UsuarioLogado usuario = jwtService.autenticar(authorization);
        usuario.exigirPerfil(Perfil.CLIENTE);
        return pagamentoService.pagar(id, request, usuario);
    }
}
