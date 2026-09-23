package br.com.chatbotmcp.mcpserver.apiclient;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Espelha o JSON devolvido por GET /pedidos/{id} e GET /clientes/{id}/pedidos na api-rest.
 */
public record PedidoApiDto(Long id, Long clienteId, LocalDate data, BigDecimal valor, String status) {
}
