package br.com.chatbotmcp.mcpserver.apiclient;

/**
 * Espelha o JSON devolvido por GET /clientes/{id} na api-rest.
 * O mcp-server nao compartilha classes com a api-rest: os dois projetos sao
 * independentes e so se falam por HTTP/JSON, entao cada lado tem seu proprio DTO.
 */
public record ClienteApiDto(Long id, String nome, String email) {
}
