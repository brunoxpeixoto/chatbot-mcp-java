package br.com.chatbotmcp.mcpserver.apiclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Unico ponto do mcp-server que fala HTTP com a api-rest. As tools (pacote
 * {@code tools}) nao sabem nada sobre URLs ou JSON de resposta: so chamam estes
 * metodos e tratam as excecoes que eles podem lancar.
 *
 * Erros esperados desta classe:
 * - org.springframework.web.client.HttpClientErrorException.NotFound: recurso nao existe (404)
 * - org.springframework.web.client.ResourceAccessException: nao conseguiu conectar (api-rest fora do ar)
 */
@Component
public class ApiRestClient {

    private final RestClient restClient;

    public ApiRestClient(RestClient.Builder restClientBuilder,
                          @Value("${api-rest.base-url}") String apiRestBaseUrl) {
        this.restClient = restClientBuilder.baseUrl(apiRestBaseUrl).build();
    }

    public ClienteApiDto buscarCliente(Long id) {
        return restClient.get()
                .uri("/clientes/{id}", id)
                .retrieve()
                .body(ClienteApiDto.class);
    }

    public List<PedidoApiDto> listarPedidosDoCliente(Long clienteId) {
        return restClient.get()
                .uri("/clientes/{id}/pedidos", clienteId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<PedidoApiDto>>() {
                });
    }

    public PedidoApiDto buscarPedido(Long id) {
        return restClient.get()
                .uri("/pedidos/{id}", id)
                .retrieve()
                .body(PedidoApiDto.class);
    }
}
