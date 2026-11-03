package br.com.raizes.model;

/** Situação do pedido e as mudanças de situação que são permitidas. */
public enum StatusPedido {
    AGUARDANDO_PAGAMENTO, PAGAMENTO_RECUSADO, PAGO, EM_PREPARO, PRONTO, ENTREGUE, CANCELADO;

    public boolean podeIrPara(StatusPedido novo) {
        return switch (this) {
            case AGUARDANDO_PAGAMENTO -> novo == CANCELADO;
            case PAGAMENTO_RECUSADO -> novo == CANCELADO;
            case PAGO -> novo == EM_PREPARO || novo == CANCELADO;
            case EM_PREPARO -> novo == PRONTO || novo == CANCELADO;
            case PRONTO -> novo == ENTREGUE;
            case ENTREGUE, CANCELADO -> false;
        };
    }
}
