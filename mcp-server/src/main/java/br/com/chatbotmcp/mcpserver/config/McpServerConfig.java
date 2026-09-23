package br.com.chatbotmcp.mcpserver.config;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.WebMvcStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import br.com.chatbotmcp.mcpserver.tools.BuscarClienteTool;
import br.com.chatbotmcp.mcpserver.tools.BuscarPedidoTool;
import br.com.chatbotmcp.mcpserver.tools.ListarPedidosClienteTool;

/**
 * Liga as duas metades do MCP Server:
 *
 * 1) O TRANSPORTE (WebMvcStreamableServerTransportProvider): expoe HTTP na URL
 *    "/mcp" e implementa o protocolo Streamable HTTP do MCP (POST para enviar
 *    mensagens JSON-RPC, GET para abrir um stream SSE, DELETE para encerrar a
 *    sessao). Ele nao sabe nada sobre clientes/pedidos — so fala JSON-RPC.
 *
 * 2) O SERVIDOR MCP (McpSyncServer): conhece as regras do protocolo MCP
 *    (initialize, tools/list, tools/call, capabilities) e delega a execucao de
 *    cada tool para o callHandler registrado em cada classe do pacote "tools".
 */
@Configuration
public class McpServerConfig {

    @Bean
    public WebMvcStreamableServerTransportProvider mcpTransportProvider() {
        return WebMvcStreamableServerTransportProvider.builder()
                .mcpEndpoint("/mcp")
                .build();
    }

    /**
     * O Spring MVC detecta automaticamente qualquer bean RouterFunction e passa a
     * rotear requisicoes para ele — e assim que o endpoint HTTP do MCP Server
     * fica "ligado" na aplicacao, sem precisar de um @RestController.
     */
    @Bean
    public RouterFunction<ServerResponse> mcpRouterFunction(
            WebMvcStreamableServerTransportProvider mcpTransportProvider) {
        return mcpTransportProvider.getRouterFunction();
    }

    @Bean(destroyMethod = "close")
    public McpSyncServer mcpSyncServer(WebMvcStreamableServerTransportProvider mcpTransportProvider,
                                        BuscarClienteTool buscarClienteTool,
                                        ListarPedidosClienteTool listarPedidosClienteTool,
                                        BuscarPedidoTool buscarPedidoTool) {
        return McpServer.sync(mcpTransportProvider)
                .serverInfo("chatbot-mcp-server", "0.1.0")
                // tools(false): a lista de tools e fixa (3 tools, definidas no codigo),
                // entao o servidor nunca precisa mandar notifications/tools/list_changed.
                .capabilities(McpSchema.ServerCapabilities.builder().tools(false).build())
                .tools(
                        buscarClienteTool.specification(),
                        listarPedidosClienteTool.specification(),
                        buscarPedidoTool.specification())
                .build();
    }
}
