package com.geekbrains.client;


import jdk.nashorn.internal.codegen.CompilerConstants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Set;

public class Network {
    private static final String SERVER_ADDR = "localhost";
    private static final int SERVER_PORT = 1000;
    private static DataInputStream in = null;
    private static DataOutputStream out = null;
    private static Socket sock = null;
    private static boolean authenticated;
    private static Thread connectionThread;
    private static Callback callOnMsgReceived;
    private static Callback callOnAuthenticated;
    private static Callback callOnException;
    private static Callback callOnCloseConnection;
    private static Callback callOnClientsListReceive;
    private static Callback callOnNickChange;

    static {
        Callback empty = (args) -> {
        };
        callOnMsgReceived = empty;
        callOnAuthenticated = empty;
        callOnException = empty;
        callOnCloseConnection = empty;
        callOnClientsListReceive = empty;
    }

    private Set<String> clients;

    public static void setCallOnClientsListReceive(Callback callOnClientsListReceive) {
        Network.callOnClientsListReceive = callOnClientsListReceive;
    }

    public static void setCallOnMsgReceived(Callback callOnMsgReceived) {
        Network.callOnMsgReceived = callOnMsgReceived;
    }

    public static void setCallOnAuthenticated(Callback callOnAuthenticated) {
        Network.callOnAuthenticated = callOnAuthenticated;
    }

    public static void setCallOnNickChange(Callback callOnNickChange) {
        Network.callOnNickChange = callOnNickChange;
    }

    public static void setCallOnException(Callback callOnException) {
        Network.callOnException = callOnException;
    }

    public static void setCallOnCloseConnection(Callback callOnCloseConnection) {
        Network.callOnCloseConnection = callOnCloseConnection;
    }

    public static boolean isConnected() {
        return !(sock == null);
    }

    public static void initializeConnection() {
        if (sock == null) {
            try {
                sock = new Socket(SERVER_ADDR, SERVER_PORT);
                in = new DataInputStream(sock.getInputStream());
                out = new DataOutputStream(sock.getOutputStream());
                System.out.println("connected to " + SERVER_ADDR + ":" + SERVER_PORT + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }

            connectionThread = new Thread(() -> {
                try {
                    while (true) {
                        String msg = in.readUTF();
                        if (msg.startsWith("/authok ")) {
                            callOnAuthenticated.callback(msg.split("\\s")[1]);
                            break;
                        }
                    }

                    while (true) {
                        String msg = in.readUTF();
                        if (msg.startsWith("/end")) {
                            System.out.println("receive /end from server");
                            break;
                        } else if (msg.startsWith("/clients ")) {
                            ObjectInputStream ob = new ObjectInputStream(in);
                            callOnClientsListReceive.callback(ob.readObject());
                        } else if (msg.startsWith("/newNick")) {
                            String[] tokens = msg.split("\\s");
                            callOnNickChange.callback(tokens);
                        } else {
                            callOnMsgReceived.callback(msg);
                        }
                    }
                } catch (Exception e) {
                    callOnException.callback("Соединение разорванно");
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            });
            connectionThread.setDaemon(true);
            connectionThread.start();
        }

    }

    public static void authorization(String login, String password) {
        try {
            out.writeUTF("/auth " + login + " " + password);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean sendMessage(String msg) {
        try {
            out.writeUTF(msg);
            out.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isAuthenticated() {
        return authenticated;
    }

    public static void setAuthenticated(boolean authenticated) {
        Network.authenticated = authenticated;
    }

    private static void closeConnection() {
        callOnCloseConnection.callback();
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            sock.close();
            sock = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void start() {

    }

    private void receiveClientsList() {
        try {
            ObjectInputStream ob = new ObjectInputStream(sock.getInputStream());
            clients = (Set<String>) ob.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
