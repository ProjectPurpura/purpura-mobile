package com.purpura.app.model.micro;

public class QrCode {
    private String key;
    private String foregroundHex;
    private String backgroundHex;
    private Integer size;

    public QrCode(String key, String foregroundHex, String backgroundHex, Integer size) {
        this.key = key;
        this.foregroundHex = foregroundHex;
        this.backgroundHex = backgroundHex;
        this.size = size;
    }

    public static class Builder {
        private String key;
        private String foregroundHex;
        private String backgroundHex;
        private Integer size;

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder foregroundHex(String foregroundHex) {
            this.foregroundHex = foregroundHex;
            return this;
        }

        public Builder backgroundHex(String backgroundHex) {
            this.backgroundHex = backgroundHex;
            return this;
        }

        public Builder size(Integer size) {
            this.size = size;
            return this;
        }

        public QrCode build() {
            return new QrCode(key, foregroundHex, backgroundHex, size);
        }
    }
}
