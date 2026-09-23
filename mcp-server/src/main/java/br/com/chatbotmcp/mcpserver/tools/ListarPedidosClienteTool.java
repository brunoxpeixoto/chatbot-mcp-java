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
 * Tool "listar_pedidos_cliente": traduz uma pergunta como "quais os pedidos do
 * cliente 123?" em GET /clientes/123/pedidos na api-rest.
 */
@Component
public class ListarPedidosClienteTool {

    private final ApiRestClient apiRestClient;
    private final ObjectMapper objectMapper;

    public ListarPedidosClienteTool(ApiRestClient apiRestClient, ObjectMapper objectMapper) {
        this.apiRestClient = apiRestClient;
        this.objectMapper = objectMapper;
    }

    public McpServerFeatures.SyncToolSpecification specification() {
        McpSchema.Tool tool = McpSchema.Tool.builder()
                .name("listar_pedidos_cliente")
                .description("""
                        Lista os pedidos de um cliente, do mais recente para o mais antigo, a \
                        partir do ID numerico do cliente. Use esta ferramenta quando o usuario \
                        perguntar sobre o historico de compras, ultimos pedidos ou pedidos de um \
                        cliente especifico, por exemplo "quais os ultimos pedidos do cliente 123?". \
                        Se o cliente nao existir, a ferramenta retorna um erro.""")
                .inputSchema(new McpSchema.JsonSchema(
                        "object",
                        Map.of("clienteId", Map.of(
                                "type", "integer",
                                "description", "ID numerico do cliente")),
                        List.of("clienteId"),
                        false, null, null))
                .build();

        return McpServerFeatures.SyncToolSpecification.builder()
                .tool(tool)
                .callHandler(this::executar)
                .build();
    }

    private McpSchema.CallToolResult executar(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        long clienteId;
        try {
            clienteId = ToolCallResults.argumentoIdObrigatorio(request.arguments(), "clienteId");
        } catch (IllegalArgumentException e) {
            return ToolCallResults.erro(e.getMessage());
        }

        try {
            List<PedidoApiDto> pedidos = apiRestClient.listarPedidosDoCliente(clienteId);
            return ToolCallResults.sucesso(paraJson(pedidos));
        } catch (HttpClientErrorException.NotFound e) {
            return ToolCallResults.erro("Cliente " + clienteId + " nao encontrado.");
        } catch (ResourceAccessException e) {
            return ToolCallResults.erro(
                    "Nao foi possivel conectar a api-rest. Ela esta rodando em http://localhost:8080?");
        } catch (Exception e) {
            return ToolCallResults.erro(
                    "Erro inesperado ao listar pedidos do cliente " + clienteId + ": " + e.getMessage());
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
