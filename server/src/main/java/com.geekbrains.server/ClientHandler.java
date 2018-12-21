package com.geekbrains.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    Socket socket;
    DataInputStream in;
    DataOutputStream out;
    Server server;
    String nick;
    Thread currentThread;

    public ClientHandler(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        try {
            this.out = new DataOutputStream(socket.getOutputStream());
            this.in = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.currentThread = new Thread(() -> {
            try {
                while (true) {
                    try {
                        String msg = in.readUTF();
                        // /auth login pass
                        if (msg.startsWith("/auth ")) {
                            String[] tokens = msg.split("\\s");
                            this.nick = server.getAuthService().getNickNameByLoginAndPassword(tokens[1], tokens[2]);
                            if (this.nick != null) {
                                // /authok authorizedNickName
                                sendMessage("/authok " + this.nick);
                                server.subscribeClient(this.nick, this);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        throw new AuthErrorException("auth error");
                    }
                }

                while (true) {
                    String msg;
                    msg = in.readUTF();
                    if (msg.equalsIgnoreCase("/end")) break;
                    else if (msg.startsWith("/w ")) {
                        String[] tokens = msg.split("\\s");
                        if (tokens.length > 1) {
                            String recieverNick = tokens[1];
                            StringBuilder msgBuilder = new StringBuilder(" :");
                            for (int i = 2; i < tokens.length; i++) {
                                msgBuilder.append(" ");
                                msgBuilder.append(tokens[i]);
                            }
                            if (server.isNickConnected(recieverNick)) {
                                server.sendPrivateMessage(recieverNick, "From " + this.nick + msgBuilder.toString());
                                server.sendPrivateMessage(this.nick, "To " + recieverNick + msgBuilder.toString());
                            } else {
                                server.sendPrivateMessage(this.nick, "message cannot be delivered");
                            }

                        }
                    } else {
                        server.sendMessage(this.nick + " : " + msg);
                    }
                    System.out.println(socket.getInetAddress().toString() + " " + msg);

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (AuthErrorException e) {
                sendMessage("/autherror");
            } finally {
                disconnect();
            }
        });
        currentThread.setDaemon(true);
        currentThread.start();
    }

    private void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        server.sendPrivateMessage(this.nick, "You are disconnected.");
        this.currentThread.interrupt();
        server.unsubscribeClient(this.nick);
        this.nick = null;
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
            System.out.println("close socket" + socket.toString());
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
