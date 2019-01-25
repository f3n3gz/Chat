package com.geekbrains.server;

public class AuthErrorException extends Exception {
    public AuthErrorException(String msg) {
        super(msg);
    }
}
