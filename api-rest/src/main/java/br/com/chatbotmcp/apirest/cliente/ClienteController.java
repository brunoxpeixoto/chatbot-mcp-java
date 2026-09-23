package br.com.chatbotmcp.apirest.cliente;

import br.com.chatbotmcp.apirest.pedido.PedidoDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("/clientes/{id}")
    public ClienteDto buscarPorId(@PathVariable Long id) {
        return ClienteDto.from(clienteService.buscarPorId(id));
    }

    @GetMapping("/clientes/{id}/pedidos")
    public List<PedidoDto> listarPedidos(@PathVariable Long id) {
        return clienteService.listarPedidosDoCliente(id).stream()
                .map(PedidoDto::from)
                .toList();
    }
}
