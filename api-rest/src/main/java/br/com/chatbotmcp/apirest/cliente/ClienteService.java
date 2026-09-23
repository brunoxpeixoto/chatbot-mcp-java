package br.com.chatbotmcp.apirest.cliente;

import br.com.chatbotmcp.apirest.common.RecursoNaoEncontradoException;
import br.com.chatbotmcp.apirest.pedido.Pedido;
import br.com.chatbotmcp.apirest.pedido.PedidoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PedidoService pedidoService;

    public ClienteService(ClienteRepository clienteRepository, PedidoService pedidoService) {
        this.clienteRepository = clienteRepository;
        this.pedidoService = pedidoService;
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente " + id + " nao encontrado"));
    }

    public List<Pedido> listarPedidosDoCliente(Long clienteId) {
        buscarPorId(clienteId);
        return pedidoService.listarPorCliente(clienteId);
    }
}
