package br.com.raizes.repository;

import br.com.raizes.model.CanalPedido;
import br.com.raizes.model.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Listagens com paginação (Pageable) e filtro por canal
    Page<Pedido> findByCanalPedido(CanalPedido canalPedido, Pageable pageable);

    Page<Pedido> findByClienteId(Long clienteId, Pageable pageable);

    Page<Pedido> findByClienteIdAndCanalPedido(Long clienteId, CanalPedido canalPedido, Pageable pageable);
}
