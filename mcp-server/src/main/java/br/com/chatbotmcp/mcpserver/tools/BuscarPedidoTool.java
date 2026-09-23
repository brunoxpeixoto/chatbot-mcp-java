package br.com.chatbotmcp.mcpserver.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import br.com.chatbotmcp.mcpserver.apiclient.ApiRestClient;
import br.com.chatbotmcp.mcpserver.apiclient.PedidoApiDto;

import java.util.List;
import java.util.Map;

/**
 * Tool "buscar_pedido": traduz uma pergunta como "qual o status do pedido 1001?"
 * em GET /pedidos/1001 na api-rest.
 */
@Component
public class BuscarPedidoTool {

    private final ApiRestClient apiRestClient;
    private final ObjectMapper objectMapper;

    public BuscarPedidoTool(ApiRestClient apiRestClient, ObjectMapper objectMapper) {
        this.apiRestClient = apiRestClient;
        this.objectMapper = objectMapper;
    }

    public McpServerFeatures.SyncToolSpecification specification() {
        McpSchema.Tool tool = McpSchema.Tool.builder()
                .name("buscar_pedido")
                .description("""
                        Busca os dados de um pedido especifico (cliente, data, valor e status) a \
                        partir do seu ID numerico. Use esta ferramenta quando o usuario perguntar \
                        sobre um pedido especifico, por exemplo "qual o status do pedido 1001?". \
                        O campo clienteId do resultado pode ser usado para buscar dados do cliente \
                        dono do pedido com a ferramenta buscar_cliente. Se o pedido nao existir, a \
                        ferramenta retorna um erro.""")
                .inputSchema(new McpSchema.JsonSchema(
                        "object",
                        Map.of("id", Map.of(
                                "type", "integer",
                                "description", "ID numerico do pedido")),
                        List.of("id"),
                        false, null, null))
                .build();

        return McpServerFeatures.SyncToolSpecification.builder()
                .tool(tool)
                .callHandler(this::executar)
                .build();
    }

    private McpSchema.CallToolResult executar(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        long id;
        try {
            id = ToolCallResults.argumentoIdObrigatorio(request.arguments(), "id");
        } catch (IllegalArgumentException e) {
            return ToolCallResults.erro(e.getMessage());
        }

        try {
            PedidoApiDto pedido = apiRestClient.buscarPedido(id);
            return ToolCallResults.sucesso(paraJson(pedido));
        } catch (HttpClientErrorException.NotFound e) {
            return ToolCallResults.erro("Pedido " + id + " nao encontrado.");
        } catch (ResourceAccessException e) {
            return ToolCallResults.erro(
                    "Nao foi possivel conectar a api-rest. Ela esta rodando em http://localhost:8080?");
        } catch (Exception e) {
            return ToolCallResults.erro("Erro inesperado ao buscar pedido " + id + ": " + e.getMessage());
        }
    }

    private String paraJson(Object valor) {
        try {
            return objectMapper.writeValueAsString(valor);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar resposta da tool", e);
        }
    }
}
