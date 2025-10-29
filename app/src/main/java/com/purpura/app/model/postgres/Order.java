package com.purpura.app.model.postgres;

public class Order {

    private Integer idPedido;
    private String idVendedor;
    private String idComprador;
    private String data;
    private String status;
    private String agendamentoColeta;
    private Double valorTotal;
    private String observacoes;

    public Order() {}

    @Override
    public String toString() {
        return "Order{" +
                "idPedido=" + idPedido +
                ", idVendedor='" + idVendedor + '\'' +
                ", idComprador='" + idComprador + '\'' +
                ", data='" + data + '\'' +
                ", status='" + status + '\'' +
                ", agendamentoColeta='" + agendamentoColeta + '\'' +
                ", valorTotal=" + valorTotal +
                ", observacoes='" + observacoes + '\'' +
                '}';
    }

    public Order(String idVendedor, String idComprador, String observacoes){
        this.idComprador = idComprador;
        this.observacoes = observacoes;
        this.idVendedor = idVendedor;
    }
    public Order(String idVendedor, String idComprador, String data, String status, String agendamentoColeta, Double valorTotal, String observacoes) {
        this.idVendedor = idVendedor;
        this.idComprador = idComprador;
        this.data = data;
        this.status = status;
        this.agendamentoColeta = agendamentoColeta;
        this.valorTotal = valorTotal;
        this.observacoes = observacoes;
    }

    public Integer getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Integer idPedido) {
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

    public void setIdComprador(String idComprador) {
        this.idComprador = idComprador;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAgendamentoColeta() {
        return agendamentoColeta;
    }

    public void setAgendamentoColeta(String agendamentoColeta) {
        this.agendamentoColeta = agendamentoColeta;
    }

    public Double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
