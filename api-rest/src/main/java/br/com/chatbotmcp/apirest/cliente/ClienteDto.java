package br.com.chatbotmcp.apirest.cliente;

public record ClienteDto(Long id, String nome, String email) {

    public static ClienteDto from(Cliente cliente) {
        return new ClienteDto(cliente.getId(), cliente.getNome(), cliente.getEmail());
    }
}
