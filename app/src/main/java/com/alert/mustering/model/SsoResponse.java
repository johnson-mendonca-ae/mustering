package com.alert.mustering.model;

public class SsoResponse {
    private boolean success;
    private SsoTokenData data;
    private int numberOfElements;
    private int totalPages;
    private int totalElements;
    private int pageNumber;
    private int pageSize;

    public SsoTokenData getData() {
        return data;
    }
}
