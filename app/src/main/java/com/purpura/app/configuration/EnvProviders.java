package com.purpura.app.configuration;

public class EnvProviders {
    public final EnvProvider MONGO_API = new EnvProvider("API_MONGO_URL");
    public final EnvProvider POSTGRES_URL = new EnvProvider("API_POSTGRES_URL");
    public final EnvProvider MICRO_URL = new EnvProvider("API_MICRO_URL");
    public final EnvProvider SITE_URL = new EnvProvider("SITE_URL");
}
