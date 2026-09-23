package br.com.chatbotmcp.chatbot.openai;

import com.openai.core.JsonValue;
import com.openai.models.FunctionDefinition;
import com.openai.models.FunctionParameters;
import com.openai.models.chat.completions.ChatCompletionFunctionTool;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * Traduz uma tool do MCP (name + description + inputSchema, no formato do
 * protocolo MCP) para o formato de "function tool" que a OpenAI entende.
 *
 * As duas representacoes de function-calling (MCP e OpenAI) sao, no fundo,
 * quase a mesma coisa — um nome, uma descricao e um JSON Schema dos
 * argumentos — porque ambas vieram da mesma ideia (o padrao de "function
 * calling" da OpenAI, que o MCP generalizou para qualquer LLM). Por isso esta
 * conversao e so uma copia de campos, sem nenhuma logica de negocio.
 */
public final class McpToolConverter {

    private McpToolConverter() {
    }

    public static ChatCompletionFunctionTool paraFunctionTool(McpSchema.Tool mcpTool) {
        McpSchema.JsonSchema schema = mcpTool.inputSchema();

        FunctionParameters.Builder parametros = FunctionParameters.builder()
                .putAdditionalProperty("type", JsonValue.from(schema.type()))
                .putAdditionalProperty("properties", JsonValue.from(schema.properties()));

        if (schema.required() != null) {
            parametros.putAdditionalProperty("required", JsonValue.from(schema.required()));
        }
        if (schema.additionalProperties() != null) {
            parametros.putAdditionalProperty("additionalProperties", JsonValue.from(schema.additionalProperties()));
        }

        return ChatCompletionFunctionTool.builder()
                .function(FunctionDefinition.builder()
                        .name(mcpTool.name())
                        .description(mcpTool.description())
                        .parameters(parametros.build())
                        .build())
                .build();
    }
}
