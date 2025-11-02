package com.purpura.app.model.postgres.order;

import java.io.Serializable;

public class OrderItem implements Serializable {

    private Long id;
    private String nome;
    private String urlFoto;
    private String idResiduo;
    private Double preco;
    private Integer quantidade;
    private Double peso;
    private String tipoUnidade;

    public OrderItem(Long id, String nome, String urlFoto, String idResiduo, Double preco, Integer quantidade, Double peso, String tipoUnidade) {
        this.id = id;
        this.nome = nome;
        this.urlFoto = urlFoto;
        this.idResiduo = idResiduo;
        this.preco = preco;
        this.quantidade = quantidade;
        this.peso = peso;
        this.tipoUnidade = tipoUnidade;
    }


    public OrderItem(String idResiduo, String urlFoto, String nome, Double preco, Integer quantidade, String tipoUnidade, Double peso) {
        this.idResiduo = idResiduo;
        this.urlFoto = urlFoto;
        this.nome = nome;
        this.preco = preco;
        this.quantidade = quantidade;
        this.tipoUnidade = tipoUnidade;
        this.peso = peso;
    }

    public OrderItem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdResiduo() {
        return idResiduo;
    }

    public void setIdResiduo(String idResiduo) {
        this.idResiduo = idResiduo;
    }

    public Double getPreco() {
        return preco;
    }

    public void setPreco(Double preco) {
        this.preco = preco;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public String getTipoUnidade() {
        return tipoUnidade;
    }

    public void setTipoUnidade(String tipoUnidade) {
        this.tipoUnidade = tipoUnidade;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getUrlFoto() {
        return urlFoto;
    }

    public void setUrlFoto(String urlFoto) {
        this.urlFoto = urlFoto;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", urlFoto='" + urlFoto + '\'' +
                ", idResiduo='" + idResiduo + '\'' +
                ", preco=" + preco +
                ", quantidade=" + quantidade +
                ", peso=" + peso +
                ", tipoUnidade='" + tipoUnidade + '\'' +
                '}';
    }
}
