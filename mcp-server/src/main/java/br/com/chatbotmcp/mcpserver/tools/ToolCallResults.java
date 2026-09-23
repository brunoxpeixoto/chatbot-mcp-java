package br.com.chatbotmcp.mcpserver.tools;

import io.modelcontextprotocol.spec.McpSchema;

import java.util.Map;

/**
 * Helpers para montar o CallToolResult que cada tool devolve em tools/call.
 *
 * O ponto central: uma tool NUNCA deve deixar uma excecao escapar do callHandler.
 * Se algo der errado (recurso nao encontrado, API fora do ar, argumento invalido),
 * a resposta ainda e um CallToolResult valido, so que com isError=true. E assim que
 * o modelo (LLM) fica sabendo que a chamada falhou e pode decidir o que fazer -
 * por exemplo, contar ao usuario que o cliente nao existe, em vez do MCP Server
 * simplesmente cair ou devolver um erro de transporte.
 */
final class ToolCallResults {

    private ToolCallResults() {
    }

    static McpSchema.CallToolResult sucesso(String textoJson) {
        return McpSchema.CallToolResult.builder()
                .addTextContent(textoJson)
                .isError(false)
                .build();
    }

    static McpSchema.CallToolResult erro(String mensagem) {
        return McpSchema.CallToolResult.builder()
                .addTextContent(mensagem)
                .isError(true)
                .build();
    }

    /**
     * Extrai um argumento numerico obrigatorio dos argumentos da chamada.
     * O JSON-RPC transporta numeros via Jackson, que os entrega como Integer/Long/Double
     * dentro do Map<String,Object> — por isso o cast generico para Number.
     *
     * Tambem aceita String numerica (ex: "123"): LLMs locais (testado com
     * llama3.2 via Ollama) nem sempre respeitam "type":"integer" do JSON
     * Schema e mandam o argumento como string. Uma tool nao deveria falhar
     * so porque o modelo foi informal sobre o tipo.
     */
    static Long argumentoIdObrigatorio(Map<String, Object> arguments, String nomeCampo) {
        Object valor = arguments == null ? null : arguments.get(nomeCampo);
        if (valor instanceof Number numero) {
            return numero.longValue();
        }
        if (valor instanceof String texto) {
            try {
                return Long.parseLong(texto.trim());
            } catch (NumberFormatException ignorado) {
                // cai no throw abaixo
            }
        }
        throw new IllegalArgumentException(
                "Argumento obrigatorio '" + nomeCampo + "' ausente ou nao numerico: " + valor);
    }
}
