package com.purpura.app.model.mongo;

public class Company {
    private String cnpj;
    private String nome;
    private String email;
    private String telefone;
    private String urlFoto;

    public Company(String cnpj, String email, String imagem, String nome, String telefone) {
        this.cnpj = cnpj;
        this.email = email;
        this.urlFoto = imagem;
        this.nome = nome;
        this.telefone = telefone;
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
