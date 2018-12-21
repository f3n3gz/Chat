package com.geekbrains.server;

public interface AuthService {
    String getNickNameByLoginAndPassword(String login, String password);
}
