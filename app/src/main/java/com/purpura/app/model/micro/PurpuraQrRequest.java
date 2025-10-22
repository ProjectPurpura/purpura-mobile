package com.purpura.app.model.micro;

public class PurpuraQrRequest {
    public static QrRequest from(String key) {
        return new QrRequest.Builder()
                .key(key)
                .foregroundHex("#000000")
                .backgroundHex("#FFFFFF")
                .size(200)
                .build();
    }
}
