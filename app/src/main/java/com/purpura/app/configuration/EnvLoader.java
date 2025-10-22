package com.purpura.app.configuration;

import android.util.Log;

public class EnvLoader {
    private final String name;
    public EnvLoader(String name) {
        this.name = name;
    }

    public String load() {
        try {
            return System.getenv(this.name);
        } catch (Exception e) {
            Log.e("EnvLoader", "Erro ao carregar variável de ambiente: " + e.getMessage());
            throw e;
        }
    }
}
