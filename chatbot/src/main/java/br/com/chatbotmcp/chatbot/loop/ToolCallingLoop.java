package br.com.chatbotmcp.chatbot.loop;

import br.com.chatbotmcp.chatbot.mcp.McpToolsClient;
import br.com.chatbotmcp.chatbot.openai.OpenAiChatClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessage;
import com.openai.models.chat.completions.ChatCompletionMessageToolCall;
import com.openai.models.chat.completions.ChatCompletionToolMessageParam;
import io.modelcontextprotocol.spec.McpSchema;

import java.util.List;
import java.util.Map;

/**
 * O coracao do chatbot: o loop de tool calling.
 *
 * A cada iteracao:
 *   1. Manda o historico (com as tools disponiveis) para o modelo.
 *   2. Se o modelo devolveu tool_calls, executa CADA UMA via MCP (tools/call)
 *      e adiciona o resultado ao historico, associado ao mesmo call_id que o
 *      modelo usou para pedir a chamada.
 *   3. Chama o modelo de novo com o historico atualizado.
 *   4. Repete ate o modelo devolver uma resposta sem tool_calls (resposta
 *      final em texto), ou ate MAX_ITERACOES ser atingido (protecao contra
 *      loop infinito, ex: o modelo insistindo em pedir a mesma tool).
 *
 * Se uma tool falhar (MCP fora do ar, JSON invalido, etc.), o erro vira texto
 * no resultado devolvido ao modelo — nunca uma excecao que interrompe o chat.
 */
public class ToolCallingLoop {

    private static final int MAX_ITERACOES = 10;

    private final OpenAiChatClient openAi;
    private final McpToolsClient mcp;
    private final boolean debug;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ToolCallingLoop(OpenAiChatClient openAi, McpToolsClient mcp, boolean debug) {
        this.openAi = openAi;
        this.mcp = mcp;
        this.debug = debug;
    }

    /**
     * @param historico builder ja populado com as tools, o system prompt e a
     *                   pergunta mais recente do usuario. E mutado durante a
     *                   execucao (cada mensagem do assistente e cada
     *                   resultado de tool sao adicionados nele).
     * @return o texto final de resposta do assistente.
     */
    public String executar(ChatCompletionCreateParams.Builder historico) {
        for (int iteracao = 1; iteracao <= MAX_ITERACOES; iteracao++) {
            if (debug) {
                int totalMensagens = historico.build().messages().size();
                System.out.println("[DEBUG] iteracao " + iteracao + "/" + MAX_ITERACOES
                        + ": enviando " + totalMensagens + " mensagem(ns) ao modelo");
            }

            ChatCompletion resposta = openAi.enviar(historico.build());
            ChatCompletionMessage mensagem = resposta.choices().get(0).message();
            historico.addMessage(mensagem);

            List<ChatCompletionMessageToolCall> toolCalls = mensagem.toolCalls().orElse(List.of());
            if (toolCalls.isEmpty()) {
                return mensagem.content().orElse("");
            }

            if (debug) {
                System.out.println("[DEBUG] modelo pediu " + toolCalls.size() + " tool call(s) nesta resposta");
            }

            for (ChatCompletionMessageToolCall toolCall : toolCalls) {
                executarUmaToolCall(toolCall, historico);
            }
        }

        return "[o modelo continuou pedindo tools mesmo apos " + MAX_ITERACOES
                + " iteracoes; parando por seguranca para evitar um loop infinito]";
    }

    private void executarUmaToolCall(ChatCompletionMessageToolCall toolCall,
                                      ChatCompletionCreateParams.Builder historico) {
        String callId = toolCall.asFunction().id();
        String nomeTool = toolCall.asFunction().function().name();
        String argumentosJson = toolCall.asFunction().function().arguments();

        if (debug) {
            System.out.println("[DEBUG] -> tools/call \"" + nomeTool + "\" (call_id=" + callId
                    + ") args=" + argumentosJson);
        }

        String resultadoTexto;
        try {
            Map<String, Object> argumentos = parseArgumentos(argumentosJson);
            McpSchema.CallToolResult callResult = mcp.chamarTool(nomeTool, argumentos);
            resultadoTexto = extrairTexto(callResult);

            if (debug) {
                System.out.println("[DEBUG] <- resultado (isError=" + callResult.isError() + "): " + resultadoTexto);
            }
        } catch (Exception e) {
            // Uma falha aqui (JSON invalido do modelo, MCP Server fora do ar,
            // etc.) vira texto de erro devolvido ao MODELO, nao uma excecao
            // que derruba o chat — o modelo pode reagir a isso (ex: pedir
            // desculpas ao usuario) na proxima iteracao.
            resultadoTexto = "Erro ao executar a tool '" + nomeTool + "': " + e.getMessage();
            if (debug) {
                System.out.println("[DEBUG] <- " + resultadoTexto);
            }
        }

        historico.addMessage(ChatCompletionToolMessageParam.builder()
                .toolCallId(callId)
                .content(resultadoTexto)
                .build());
    }

    private Map<String, Object> parseArgumentos(String argumentosJson) throws Exception {
        if (argumentosJson == null || argumentosJson.isBlank()) {
            return Map.of();
        }
        return objectMapper.readValue(argumentosJson, new TypeReference<Map<String, Object>>() {
        });
    }

    private String extrairTexto(McpSchema.CallToolResult resultado) {
        StringBuilder texto = new StringBuilder();
        for (McpSchema.Content conteudo : resultado.content()) {
            if (conteudo instanceof McpSchema.TextContent textContent) {
                if (!texto.isEmpty()) {
                    texto.append("\n");
                }
                texto.append(textContent.text());
            }
        }
        return texto.toString();
    }
}
