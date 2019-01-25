package com.geekbrains.server.DBClasses;

import java.sql.*;

public class DBUsers {
    private static Connection connection;
    private static Statement stm;
    private static PreparedStatement psUpdate;
    private static PreparedStatement psGetUserNickName;
    private static PreparedStatement psInsert;
    private static PreparedStatement psChangeNickName;

    private DBUsers() {

    }

    public static void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:GBChat.db");
        stm = connection.createStatement();
        initializeDB();
        prepareStatement();
        fill();
    }

    private static void initializeDB() throws SQLException {
        stm.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "login TEXT," +
                "password TEXT," +
                "nickName TEXT,  " +
                "CONSTRAINT LOGIN_PASSWORD UNIQUE (login,password));");

    }

    private static void prepareStatement() throws SQLException {
        psInsert = connection.prepareStatement("INSERT INTO users (login,password,nickName) values (?,?,?);");
        psGetUserNickName = connection.prepareStatement("SELECT nickName from users where login=? and password=?");
        psChangeNickName = connection.prepareStatement("UPDATE users SET nickName=? WHERE login=? and password=?");
    }

    private static void fill() throws SQLException {
        for (int i = 0; i < 10; i++) {
            psInsert.setString(1, "login" + i);
            psInsert.setString(2, "password" + i);
            psInsert.setString(3, "nickName" + i);
            psInsert.addBatch();
        }
        psInsert.executeBatch();
    }

    public static String getNickByLoginAndPassword(String login, String password) throws SQLException {
        psGetUserNickName.setString(1, login);
        psGetUserNickName.setString(2, password);
        ResultSet rs = psGetUserNickName.executeQuery();
        while (rs.next()) {
            return rs.getString(1);
        }
        return null;
    }

    public static String changeNickNameByLoginAndPassword(String login, String password, String newNickName) {
        try {
            psChangeNickName.setString(1, newNickName);
            psChangeNickName.setString(2, login);
            psChangeNickName.setString(3, password);
            if (psChangeNickName.executeUpdate() == 1) {
                return newNickName;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void disconnect() {
        try {
            stm.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
