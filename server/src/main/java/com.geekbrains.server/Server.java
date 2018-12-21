package com.geekbrains.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Vector;

public class Server implements Runnable {
    ServerSocket serverSocket;
    Socket socket;
    int port;
    volatile boolean END_FLAG = false;

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
            String msg = "";
            while (true) {
                try {
                    msg = in.readLine();
                    if (msg.equalsIgnoreCase("/end")) {
                        END_FLAG = true;
                        System.out.println("end");
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

    public void sendMessage(String msg) {
        for (ClientHandler clientHandler : clientHandlers.values()) {
            try {
                clientHandler.out.writeUTF(msg);
                clientHandler.out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendPrivateMessage(String nick, String msg) {
        try {
            clientHandlers.get(nick).out.writeUTF(msg);
            clientHandlers.get(nick).out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isNickConnected(String nick) {
        return clientHandlers.keySet().contains((String) nick);
    }

    public void subscribeClient(String nickName, ClientHandler clientHandler) {
        if (clientHandlers.containsKey(nickName)) {
            clientHandlers.get(nickName).disconnect();
        }
        clientHandlers.put(nickName, clientHandler);
    }

    public void unsubscribeClient(String nick) {
        clientHandlers.remove(nick);
    }

    public SimpleAuthService getAuthService() {
        return authService;
    }

    public boolean isEND_FLAG() {
        return END_FLAG;
    }
}
