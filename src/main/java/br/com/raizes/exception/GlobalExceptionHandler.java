package br.com.raizes.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

/**
 * Captura os erros de toda a API e responde sempre com o mesmo JSON (ErroResponse).
 * Cada método abaixo trata um tipo de erro.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Nossos erros de regra de negócio (401, 403, 404, 409, 422...). */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErroResponse> regraDeNegocio(ApiException ex, HttpServletRequest req) {
        return resposta(ex.getStatus(), ex.getCodigo(), ex.getMessage(), ex.getDetalhes(), req);
    }

    /** Dados inválidos (campo obrigatório ausente, quantidade negativa, e-mail inválido...): 422. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ErroResponse.Detalhe> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> new ErroResponse.Detalhe(erro.getField(), erro.getDefaultMessage()))
                .toList();
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDACAO_INVALIDA", "Há campos inválidos na requisição.", detalhes, req);
    }

    /** JSON malformado ou valor que não existe no enum (ex.: canalPedido = "XYZ"): 400. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> jsonInvalido(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, "REQUISICAO_INVALIDA",
                "Corpo da requisição inválido. Verifique o JSON e os valores permitidos (ex.: canalPedido).", List.of(), req);
    }

    /** Parâmetro da URL com valor inválido (ex.: ?canalPedido=XYZ): 400. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> parametroInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, "PARAMETRO_INVALIDO", "Valor inválido para o parâmetro '" + ex.getName() + "'.", List.of(), req);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> rotaInexistente(NoResourceFoundException ex, HttpServletRequest req) {
        return resposta(HttpStatus.NOT_FOUND, "ROTA_NAO_ENCONTRADA", "Rota não encontrada.", List.of(), req);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> metodoNaoPermitido(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED, "METODO_NAO_PERMITIDO", "Método HTTP não permitido nesta rota.", List.of(), req);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResponse> tipoNaoSuportado(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        return resposta(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "TIPO_NAO_SUPORTADO", "Envie o corpo como JSON (Content-Type: application/json).", List.of(), req);
    }

    /** Qualquer outro erro inesperado: 500, sem mostrar detalhes internos. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> erroInesperado(Exception ex, HttpServletRequest req) {
        log.error("Erro inesperado em {}", req.getRequestURI(), ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO", "Ocorreu um erro inesperado.", List.of(), req);
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, String codigo, String mensagem,
                                                  List<ErroResponse.Detalhe> detalhes, HttpServletRequest req) {
        return ResponseEntity.status(status).body(ErroResponse.de(codigo, mensagem, detalhes, req.getRequestURI()));
    }
}
