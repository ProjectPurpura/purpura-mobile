package com.purpura.app.model.mongo;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class PixKey implements Serializable {
    private String id;

    @SerializedName("nome")
    private String name;

    @SerializedName("chave")
    private String key;

    public PixKey(String name, String key, String id) {
        this.name = name;
        this.key = key;
        this.id = id;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
}
