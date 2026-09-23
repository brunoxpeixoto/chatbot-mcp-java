package br.com.chatbotmcp.chatbot;

import br.com.chatbotmcp.chatbot.loop.ToolCallingLoop;
import br.com.chatbotmcp.chatbot.mcp.McpToolsClient;
import br.com.chatbotmcp.chatbot.openai.McpToolConverter;
import br.com.chatbotmcp.chatbot.openai.OpenAiChatClient;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import io.modelcontextprotocol.spec.McpSchema;

import java.util.List;
import java.util.Scanner;

/**
 * Etapa 8: o chatbot completo — MCP Client + OpenAI + loop de tool calling.
 *
 * Fluxo de inicializacao:
 *   1. Conecta ao MCP Server (initialize + tools/list) e cacheia as tools.
 *   2. Converte cada tool MCP para o formato de "function tool" da OpenAI.
 *   3. Monta o historico inicial (system prompt + tools).
 *
 * Fluxo por pergunta:
 *   4. Le a pergunta do usuario, adiciona ao historico.
 *   5. Delega ao ToolCallingLoop, que conversa com o modelo e executa as
 *      tools que ele pedir, quantas vezes forem necessarias.
 *   6. Mostra a resposta final; o historico continua acumulado para a
 *      proxima pergunta.
 */
public class Main {

    private static final String MODEL = System.getenv().getOrDefault("OPENAI_MODEL", "llama3.2");
    // Configuravel via env var para funcionar tanto rodando local (localhost)
    // quanto em Docker Compose (onde o mcp-server e alcancado pelo nome do
    // servico, ex: http://mcp-server:8081).
    private static final String MCP_SERVER_URL =
            System.getenv().getOrDefault("MCP_SERVER_URL", "http://localhost:8081");
    private static final String MCP_ENDPOINT = "/mcp";

    public static void main(String[] args) {
        boolean debug = List.of(args).contains("--debug");

        try (McpToolsClient mcp = new McpToolsClient(MCP_SERVER_URL, MCP_ENDPOINT)) {
            System.out.println("Conectando ao MCP Server em " + MCP_SERVER_URL + MCP_ENDPOINT + " ...");
            McpSchema.InitializeResult init = mcp.conectar();
            List<McpSchema.Tool> mcpTools = mcp.listarTools();
            System.out.println("Conectado a '" + init.serverInfo().name() + "'. Tools disponiveis: "
                    + mcpTools.stream().map(McpSchema.Tool::name).toList());

            OpenAiChatClient openAi = new OpenAiChatClient();
            ToolCallingLoop loop = new ToolCallingLoop(openAi, mcp, debug);

            ChatCompletionCreateParams.Builder historico = ChatCompletionCreateParams.builder()
                    .model(MODEL)
                    .addSystemMessage("Voce e um assistente que responde perguntas sobre clientes e "
                            + "pedidos de uma loja. Sempre que a pergunta envolver um cliente ou "
                            + "pedido especifico (por ID), use as ferramentas disponiveis para buscar "
                            + "os dados reais em vez de inventar. Se uma ferramenta disser que o "
                            + "recurso nao foi encontrado, informe isso ao usuario claramente.");

            for (McpSchema.Tool tool : mcpTools) {
                historico.addTool(McpToolConverter.paraFunctionTool(tool));
            }

            System.out.println();
            System.out.println("Pronto! Exemplos de pergunta:");
            System.out.println("  Quem e o cliente 123 e quais sao os ultimos pedidos dele?");
            System.out.println("  Qual o status do pedido 1001 e quem e o cliente dele?");
            System.out.println("Digite 'sair' para encerrar.");

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) {
                    break;
                }
                String pergunta = scanner.nextLine().trim();
                if (pergunta.equalsIgnoreCase("sair")) {
                    break;
                }
                if (pergunta.isEmpty()) {
                    continue;
                }

                historico.addUserMessage(pergunta);
                String respostaFinal = loop.executar(historico);
                System.out.println(respostaFinal);
            }
        } catch (Exception e) {
            System.err.println("Erro fatal: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
