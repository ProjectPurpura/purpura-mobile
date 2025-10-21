package com.purpura.app.model.postgres;

import java.time.LocalDateTime;

public class Order {

    private Long idPedido;
    private String idVendedor;
    private String idComprador;
    private Double valorTotal;
    private String observacoes;
    private LocalDateTime data;
    private String status;
    private LocalDateTime agendamentoColeta;

    public Long getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Long idPedido) {
        this.idPedido = idPedido;
    }

    public String getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(String idVendedor) {
        this.idVendedor = idVendedor;
    }

    public String getIdComprador() {
        return idComprador;
    }

    public Double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public void setIdComprador(String idComprador) {
        this.idComprador = idComprador;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
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

    public LocalDateTime getAgendamentoColeta() {
        return agendamentoColeta;
    }

    public void setAgendamentoColeta(LocalDateTime agendamentoColeta) {
        this.agendamentoColeta = agendamentoColeta;
    }

    public Order(Long idPedido, String idVendedor, String idComprador, String observacoes, LocalDateTime data, String status, LocalDateTime agendamentoColeta) {
        this.idPedido = idPedido;
        this.idVendedor = idVendedor;
        this.idComprador = idComprador;
        this.observacoes = observacoes;
        this.data = data;
        this.status = status;
        this.agendamentoColeta = agendamentoColeta;
    }

    public Order() {
    }
}
