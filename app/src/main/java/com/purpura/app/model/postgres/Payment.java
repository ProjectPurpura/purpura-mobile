package com.purpura.app.model.postgres;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {

    private Long idPagamento;
    private Long idPedido;
    private BigDecimal valorPago;
    private String tipo;
    private LocalDateTime data;
    private String status;

    public Payment(){}

    public Payment(Long idPagamento, Long idPedido, BigDecimal valorPago, String tipo, LocalDateTime data, String status) {
        this.idPagamento = idPagamento;
        this.idPedido = idPedido;
        this.valorPago = valorPago;
        this.tipo = tipo;
        this.data = data;
        this.status = status;
    }

    public Long getIdPagamento() {
        return idPagamento;
    }

    public void setIdPagamento(Long idPagamento) {
        this.idPagamento = idPagamento;
    }

    public BigDecimal getValorPago() {
        return valorPago;
    }

    public void setValorPago(BigDecimal valorPago) {
        this.valorPago = valorPago;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
