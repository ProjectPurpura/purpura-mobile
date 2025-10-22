package com.purpura.app.configuration;

public class EnvProvider {
    private final String name;
    public EnvProvider(String name) {
        this.name = name;
    }

    public String load() {
        return System.getenv(this.name);
    }
}
