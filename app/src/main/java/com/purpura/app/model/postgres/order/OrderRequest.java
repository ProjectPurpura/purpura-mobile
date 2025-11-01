package com.purpura.app.model.postgres.order;

public class OrderRequest {
    private String idVendedor;
    private String idComprador;
    private String obsevacoes;

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

    public String getObsevacoes() {
        return obsevacoes;
    }

    public void setObsevacoes(String obsevacoes) {
        this.obsevacoes = obsevacoes;
    }

    @Override
    public String toString() {
        return "OrderRequest{" +
                "idVendedor='" + idVendedor + '\'' +
                ", idComprador='" + idComprador + '\'' +
                ", obsevacoes='" + obsevacoes + '\'' +
                '}';
    }

    public OrderRequest(String idVendedor, String idComprador, String obsevacoes) {
        this.idVendedor = idVendedor;
        this.idComprador = idComprador;
        this.obsevacoes = obsevacoes;
    }
}
