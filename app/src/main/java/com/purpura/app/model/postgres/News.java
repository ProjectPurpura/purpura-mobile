package com.purpura.app.model.postgres;

public class News {

    private Long id;
    private String titulo;
    private String link;
    private String urlImagem;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getUrlImagem() {
        return this.urlImagem;
    }

    public void setUrlImagem(String urlImagem) {
        this.urlImagem = urlImagem;
    }

    public News(){};

    public News(Long id, String titulo, String link, String urlImagem) {
        this.id = id;
        this.titulo = titulo;
        this.link = link;
        this.urlImagem = urlImagem;
    }
}

