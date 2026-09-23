package br.com.chatbotmcp.chatbot.openai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

/**
 * Fina camada sobre o client oficial da OpenAI (Chat Completions API).
 *
 * Este client e "sem estado": ele nao guarda o historico da conversa. Quem
 * acumula o historico entre chamadas e o ChatCompletionCreateParams.Builder
 * mantido pelo chamador (Main) — a cada pergunta nova, o historico INTEIRO e
 * reenviado. O modelo em si nao lembra de nada entre requisicoes HTTP.
 *
 * Por que Chat Completions e nao Responses API (a API "principal" da OpenAI,
 * ver Etapa 7 original): este projeto roda contra um Ollama local (para nao
 * precisar de cartao de credito), e o endpoint OpenAI-compativel do Ollama
 * (e da maioria dos runtimes locais) so implementa /v1/chat/completions, nao
 * /v1/responses. O SDK oficial da OpenAI suporta as duas, entao a troca e so
 * de qual API chamar — o restante do fluxo (historico, tool calling) e
 * conceitualmente o mesmo.
 */
public class OpenAiChatClient {

    private final OpenAIClient client;

    public OpenAiChatClient() {
        // Le OPENAI_API_KEY e OPENAI_BASE_URL do ambiente. Apontando
        // OPENAI_BASE_URL para http://localhost:11434/v1, o MESMO codigo fala
        // com um Ollama local em vez da nuvem da OpenAI — e por isso que vale
        // a pena usar o SDK oficial mesmo com um provedor diferente.
        this.client = OpenAIOkHttpClient.fromEnv();
    }

    public ChatCompletion enviar(ChatCompletionCreateParams params) {
        return client.chat().completions().create(params);
    }
}
