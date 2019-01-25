package com.geekbrains.server;

import com.geekbrains.server.DBClasses.DBUsers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements AuthService {

    @Override
    public String getNickNameByLoginAndPassword(String login, String password) {
        try {
            return DBUsers.getNickByLoginAndPassword(login, password);
        } catch (SQLException e) {
            return null;
        }
    }
}
