package com.purpura.app.model.mongo;

import java.io.Serializable;

public class PixKey implements Serializable {
    private String name;
    private String key;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public PixKey(String name, String key, String id) {
        this.name = name;
        this.key = key;
        this.id = id;
    }

    @Override
    public String toString() {
        return "PixKey{" +
                "name='" + name + '\'' +
                ", key='" + key + '\'' +
                ", id='" + id + '\'' +
                '}';
    }

    public PixKey(String name, String key) {
        this.name = name;
        this.key = key;
    }
}
