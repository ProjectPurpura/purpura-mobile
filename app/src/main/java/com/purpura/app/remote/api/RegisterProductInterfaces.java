package com.purpura.app.remote.api;

public interface RegisterProductInterfaces {

    public interface PixKeyCallback {
        void onSuccess(String id);
        void onError(Throwable t);
    }

    public interface AdressCallback {
        void onSuccess(String id);
        void onError(Throwable t);
    }

    public interface ResidueCallback {
        void onSuccess(String id);
        void onError(Throwable t);
    }

}
