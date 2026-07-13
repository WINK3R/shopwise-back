package com.example.shopwiseapi.service;

public class InvitationExpiredException extends RuntimeException {

    public InvitationExpiredException(String message) {
        super(message);
    }
}
