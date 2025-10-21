package com.purpura.app.model.mongo;

public class Company {
    private String cnpj;
    private String email;
    private String urlFoto;
    private String nome;
    private String telefone;


    public Company(String cnpj, String nome, String email, String telefone, String imagem ) {
        this.cnpj = cnpj;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
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
