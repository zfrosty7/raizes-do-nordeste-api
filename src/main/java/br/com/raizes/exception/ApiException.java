package br.com.raizes.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Exceção usada para TODOS os erros de regra de negócio. Exemplo:
 * throw new ApiException(HttpStatus.NOT_FOUND, "PEDIDO_NAO_ENCONTRADO", "Pedido não encontrado.");
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String codigo;
    private final List<ErroResponse.Detalhe> detalhes;

    public ApiException(HttpStatus status, String codigo, String mensagem) {
        this(status, codigo, mensagem, List.of());
    }

    public ApiException(HttpStatus status, String codigo, String mensagem, List<ErroResponse.Detalhe> detalhes) {
        super(mensagem);
        this.status = status;
        this.codigo = codigo;
        this.detalhes = detalhes;
    }

    public HttpStatus getStatus() { return status; }
    public String getCodigo() { return codigo; }
    public List<ErroResponse.Detalhe> getDetalhes() { return detalhes; }
}
