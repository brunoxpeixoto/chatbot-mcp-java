package br.com.chatbotmcp.apirest.pedido;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long clienteId;

    private LocalDate data;

    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    private StatusPedido status;

    protected Pedido() {
        // exigido pelo JPA
    }

    public Pedido(Long clienteId, LocalDate data, BigDecimal valor, StatusPedido status) {
        this.clienteId = clienteId;
        this.data = data;
        this.valor = valor;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public LocalDate getData() {
        return data;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public StatusPedido getStatus() {
        return status;
    }
}
