package com.geekbrains.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Server implements Runnable {
    final Object mon = new Object();
    private ServerSocket serverSocket;
    private Socket socket;
    private int port;

    private HashMap<String, ClientHandler> clientHandlers = new HashMap<>();
    private SimpleAuthService authService;

    public Server(int port) {
        this.port = port;
        authService = new SimpleAuthService();
    }

    public void run() {

        Thread server = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                System.out.println("Server started on port 1000. Waiting for connection");
                while (true) {
                    socket = serverSocket.accept();
                    System.out.println("Client connected " + socket.getInetAddress().toString());
                    ClientHandler clientHandler = new ClientHandler(this, socket);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        server.setDaemon(true);
        server.start();
        serverOutput();
    }


    private void serverOutput() {

        //поток для чтения консоли на серевере
        Thread serverOutput = new Thread(() -> {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String msg;
            while (true) {
                try {
                    msg = in.readLine();
                    if (msg.equalsIgnoreCase("/end")) {
                        System.out.println("end");
                        synchronized (this.mon) {
                            this.mon.notify();
                        }
                    } else {
                        sendMessage(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        serverOutput.setDaemon(true);
        serverOutput.start();
    }

    void sendMessage(String msg) {
        for (ClientHandler clientHandler : clientHandlers.values()) {
            try {
                clientHandler.getOut().writeUTF(msg);
                clientHandler.getOut().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void sendPrivateMessage(String nick, String msg) {
        try {
            clientHandlers.get(nick).getOut().writeUTF(msg);
            clientHandlers.get(nick).getOut().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void broadcastClientsList() {
        for (ClientHandler clientHandler : clientHandlers.values()) {
            try {
                clientHandler.getOut().writeUTF("/clients ");
                ObjectOutputStream ob = new ObjectOutputStream(clientHandler.socket.getOutputStream());
                ob.writeObject(new HashSet<>(clientHandlers.keySet()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    boolean isNickConnected(String nick) {
        return clientHandlers.keySet().contains(nick);
    }

    void subscribeClient(String nickName, ClientHandler clientHandler) {
        if (clientHandlers.containsKey(nickName)) {
            clientHandlers.get(nickName).disconnect();
        }
        clientHandlers.put(nickName, clientHandler);
        broadcastClientsList();
    }

    void unsubscribeClient(String nick) {
        clientHandlers.remove(nick);
        broadcastClientsList();
    }

    SimpleAuthService getAuthService() {
        return authService;
    }
}
