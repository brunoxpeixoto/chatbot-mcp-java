package br.com.chatbotmcp.apirest.pedido;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping("/pedidos/{id}")
    public PedidoDto buscarPorId(@PathVariable Long id) {
        return PedidoDto.from(pedidoService.buscarPorId(id));
    }
}
