package com.purpura.app.configuration;

public class EnvLoader {
    private final String name;
    public EnvLoader(String name) {
        this.name = name;
    }

    public String load() {
        return System.getenv(this.name);
    }
}
