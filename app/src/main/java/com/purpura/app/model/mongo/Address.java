package com.purpura.app.model.mongo;

import java.io.Serializable;

public class Address implements Serializable {

    private String nome;
    private String cep;
    private String complement;
    private int number;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getComplement() {
        return complement;
    }

    public void setComplement(String complement) {
        this.complement = complement;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Address(String id, String nome, String cep, String complement, int number) {
        this.id = id;
        this.nome = nome;
        this.cep = cep;
        this.complement = complement;
        this.number = number;

    }
}