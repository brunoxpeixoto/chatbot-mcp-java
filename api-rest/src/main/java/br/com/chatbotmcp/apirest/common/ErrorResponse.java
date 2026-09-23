package br.com.chatbotmcp.apirest.common;

import java.time.Instant;

public record ErrorResponse(String mensagem, int status, Instant timestamp) {

    public static ErrorResponse of(String mensagem, int status) {
        return new ErrorResponse(mensagem, status, Instant.now());
    }
}
