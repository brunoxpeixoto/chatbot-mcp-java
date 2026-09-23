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
import br.com.chatbotmcp.mcpserver.apiclient.ClienteApiDto;

import java.util.List;
import java.util.Map;

/**
 * Tool "buscar_cliente": traduz uma pergunta como "quem e o cliente 123?" em
 * GET /clientes/123 na api-rest.
 */
@Component
public class BuscarClienteTool {

    private final ApiRestClient apiRestClient;
    private final ObjectMapper objectMapper;

    public BuscarClienteTool(ApiRestClient apiRestClient, ObjectMapper objectMapper) {
        this.apiRestClient = apiRestClient;
        this.objectMapper = objectMapper;
    }

    public McpServerFeatures.SyncToolSpecification specification() {
        McpSchema.Tool tool = McpSchema.Tool.builder()
                .name("buscar_cliente")
                .description("""
                        Busca os dados cadastrais (nome e email) de um cliente a partir do seu ID \
                        numerico. Use esta ferramenta sempre que o usuario perguntar sobre um \
                        cliente especifico, por exemplo "quem e o cliente 123?" ou "qual o email \
                        do cliente 42?". Se o ID nao existir, a ferramenta retorna um erro \
                        explicando isso — nesse caso, informe ao usuario que o cliente nao foi \
                        encontrado em vez de inventar dados.""")
                .inputSchema(new McpSchema.JsonSchema(
                        "object",
                        Map.of("id", Map.of(
                                "type", "integer",
                                "description", "ID numerico do cliente")),
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
            ClienteApiDto cliente = apiRestClient.buscarCliente(id);
            return ToolCallResults.sucesso(paraJson(cliente));
        } catch (HttpClientErrorException.NotFound e) {
            return ToolCallResults.erro("Cliente " + id + " nao encontrado.");
        } catch (ResourceAccessException e) {
            return ToolCallResults.erro(
                    "Nao foi possivel conectar a api-rest. Ela esta rodando em http://localhost:8080?");
        } catch (Exception e) {
            return ToolCallResults.erro("Erro inesperado ao buscar cliente " + id + ": " + e.getMessage());
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
