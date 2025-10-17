package com.purpura.app.model;

public class Company {
    private String cnpj;
    private String telefone;
    private String email;
    private String nome;
    private String urlFoto;


    public Company(String cnpj, String telefone, String email, String nome, String imagem) {
        this.cnpj = cnpj;
        this.telefone = telefone;
        this.email = email;
        this.nome = nome;
        this.urlFoto = imagem;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getUrlFoto() {
        return urlFoto;
    }
    public void setUrlFoto(String urlFoto) {
        this.urlFoto = urlFoto;
    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPhone() {
        return telefone;
    }
    public void setPhone(String phone) {
        this.telefone = phone;
    }


    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }
}
