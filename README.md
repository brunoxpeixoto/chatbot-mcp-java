# chatbot-mcp

Projeto de aprendizado: como um LLM usa ferramentas (tool calling) por meio de um **MCP Server**, construído em três aplicações Java independentes.

Documentação completa (diagrama de fluxo, glossário dos conceitos e checklist de produção) em [ENTREGA-FINAL.md](ENTREGA-FINAL.md).

## Estrutura

```
chatbot-mcp/
├── api-rest/      # Spring Boot + JPA + H2. Serve clientes/pedidos via HTTP. Não sabe nada de MCP/LLM.
├── mcp-server/     # Spring Boot. Expõe 3 tools MCP que chamam a api-rest por baixo.
└── chatbot/        # Java puro (sem Spring). Console interativo: MCP Client + OpenAI SDK + loop de tool calling.
```

| App | Porta | O que é |
|---|---|---|
| `api-rest` | 8080 | API REST comum (`GET /clientes/{id}`, `GET /clientes/{id}/pedidos`, `GET /pedidos/{id}`) |
| `mcp-server` | 8081 | MCP Server (Streamable HTTP, endpoint `/mcp`) com as tools `buscar_cliente`, `listar_pedidos_cliente`, `buscar_pedido` |
| `chatbot` | — | CLI interativo que conecta no MCP Server e conversa com um LLM (OpenAI ou Ollama local) |

## Pré-requisitos

- Java 21 (testado com `21.0.7-oracle`, via SDKMAN)
- Maven 3.9.9
- Um LLM configurado — duas opções:
  - **Ollama local (grátis, sem cartão)**: `ollama serve` + `ollama pull llama3.1`
  - **OpenAI de verdade**: uma `OPENAI_API_KEY` válida

## Rodando localmente (sem Docker)

Em três terminais separados, na ordem:

```bash
# 1. api-rest
cd api-rest && mvn spring-boot:run
```

```bash
# 2. mcp-server (depende da api-rest estar no ar)
cd mcp-server && mvn spring-boot:run
```

```bash
# 3. chatbot (depende do mcp-server estar no ar)
export OPENAI_API_KEY="ollama"                          # ou sua chave real da OpenAI
export OPENAI_BASE_URL="http://localhost:11434/v1"       # ou https://api.openai.com/v1
export OPENAI_MODEL="llama3.1"                           # ou gpt-5.1-mini, etc.

cd chatbot
mvn -q clean package
java -jar target/chatbot-0.0.1-SNAPSHOT.jar --debug
```

A flag `--debug` imprime cada passo: o que é enviado ao modelo, cada tool call pedida com seus argumentos, e cada resultado vindo do MCP Server.

## Rodando com Docker Compose (opcional)

```bash
cp .env.example .env      # ajuste se for usar OpenAI em vez de Ollama
docker compose up -d api-rest mcp-server
docker compose run --rm chatbot --debug
```

O Ollama fica de fora do compose de propósito (roda nativamente no host, via `host.docker.internal`) — assim mantém aceleração de GPU e não duplica o download dos modelos.

## Testando a api-rest e o mcp-server isoladamente

```bash
# api-rest
curl http://localhost:8080/clientes/123
curl http://localhost:8080/clientes/123/pedidos
curl http://localhost:8080/pedidos/1001

# mcp-server, com o MCP Inspector (precisa de Node >= 22)
npx @modelcontextprotocol/inspector@latest --cli --server-url http://localhost:8081/mcp --method tools/list
```

## Exemplos de pergunta para o chatbot

- "Quem é o cliente 123 e quais são os últimos pedidos dele?"
- "Qual o status do pedido 1001 e quem é o cliente dele?"
- "Quem é o cliente 999999?" (não existe — testa o tratamento de erro)
