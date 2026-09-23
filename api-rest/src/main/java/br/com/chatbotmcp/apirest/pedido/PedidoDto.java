package br.com.chatbotmcp.apirest.pedido;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PedidoDto(Long id, Long clienteId, LocalDate data, BigDecimal valor, StatusPedido status) {

    public static PedidoDto from(Pedido pedido) {
        return new PedidoDto(
                pedido.getId(),
                pedido.getClienteId(),
                pedido.getData(),
                pedido.getValor(),
                pedido.getStatus());
    }
}
