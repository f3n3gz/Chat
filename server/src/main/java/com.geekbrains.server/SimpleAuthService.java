package com.geekbrains.server;

import com.geekbrains.server.DBClasses.DBUsers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements AuthService {

//    private List<UserDate> users;
//
//    public SimpleAuthService() {
//        users = new ArrayList<>();
//        for (int i = 0; i < 10; i++) {
//            users.add(new UserDate("login" + i, "pass" + i, "nick" + i));
//        }
//    }

    @Override
    public String getNickNameByLoginAndPassword(String login, String password) {
//        for (UserDate userDate : users
//        ) {
//            if (userDate.login.equals(login) && userDate.password.equals(password))
//                return userDate.nickname;
//        }
//        return null;
        try {
            return DBUsers.getNickByLoginAndPassword(login, password);
        } catch (SQLException e) {
            return null;
        }
    }

//    private class UserDate {
//        private String login;
//        private String password;
//        private String nickname;
//
//        public UserDate(String login, String password, String nickname) {
//            this.login = login;
//            this.password = password;
//            this.nickname = nickname;
//        }
//    }
}
