package br.com.chatbotmcp.chatbot.mcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;

import java.util.List;
import java.util.Map;

/**
 * Fina camada sobre o McpSyncClient do SDK oficial do MCP: conecta a um MCP
 * Server via Streamable HTTP, faz o handshake (initialize) e expoe
 * listarTools()/chamarTool().
 *
 * Esta classe e o "MCP Client" propriamente dito. O resto do chatbot (o loop
 * de tool calling com a OpenAI, nas proximas etapas) so enxerga estes 3
 * metodos — nao sabe nada sobre JSON-RPC, sessao HTTP ou o protocolo MCP.
 */
public class McpToolsClient implements AutoCloseable {

    private final McpSyncClient client;

    public McpToolsClient(String baseUrl, String mcpEndpoint) {
        HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport
                .builder(baseUrl)
                .endpoint(mcpEndpoint)
                .build();

        this.client = McpClient.sync(transport)
                .clientInfo(new McpSchema.Implementation("chatbot-mcp-client", "0.1.0"))
                .build();
    }

    /**
     * Faz o handshake MCP: envia "initialize" e recebe de volta a versao do
     * protocolo negociada e as capabilities do servidor. O SDK cuida de mandar
     * a notification "notifications/initialized" logo em seguida.
     */
    public McpSchema.InitializeResult conectar() {
        return client.initialize();
    }

    /** Chama "tools/list" no servidor. */
    public List<McpSchema.Tool> listarTools() {
        return client.listTools().tools();
    }

    /** Chama "tools/call" no servidor para a tool e argumentos informados. */
    public McpSchema.CallToolResult chamarTool(String nome, Map<String, Object> argumentos) {
        return client.callTool(new McpSchema.CallToolRequest(nome, argumentos));
    }

    @Override
    public void close() {
        client.closeGracefully();
    }
}
