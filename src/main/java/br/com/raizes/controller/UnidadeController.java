package br.com.raizes.controller;

import br.com.raizes.model.Produto;
import br.com.raizes.security.JwtService;
import br.com.raizes.service.CardapioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/unidades")
@Tag(name = "Unidades", description = "Cardápio por unidade")
@SecurityRequirement(name = "bearerAuth")
public class UnidadeController {

    private final CardapioService cardapioService;
    private final JwtService jwtService;

    public UnidadeController(CardapioService cardapioService, JwtService jwtService) {
        this.cardapioService = cardapioService;
        this.jwtService = jwtService;
    }

    @GetMapping("/{id}/cardapio")
    @Operation(summary = "Consulta o cardápio (produtos e estoque) de uma unidade")
    public List<Produto> cardapio(@PathVariable Long id,
                                  @RequestHeader(value = "Authorization", required = false) String authorization) {
        jwtService.autenticar(authorization);   // 401 se não tiver token válido
        return cardapioService.cardapioDaUnidade(id);
    }
}
