package com.geekbrains.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ClientHandler {
    Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Server server;
    private String nick;
    private Thread currentThread;



    public ClientHandler(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        try {
            this.out = new DataOutputStream(socket.getOutputStream());
            this.in = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }


        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Disconnect " + socket.toString());
                if (nick == null) {
                    disconnect();
                    timer.cancel();
                }
            }
        }, 120 * 1000);


        this.currentThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {

                    try {
                        String msg = in.readUTF();
                        // /auth login pass
                        if (msg.startsWith("/auth ")) {
                            String[] tokens = msg.split("\\s");
                            if (tokens.length > 2) {
                                this.nick = server.getAuthService().getNickNameByLoginAndPassword(tokens[1], tokens[2]);
                            }
                            if (this.nick != null) {
                                // /authok authorizedNickName
                                System.out.println("/authok " + this.nick);
                                sendMessage("/authok " + this.nick);
                                server.subscribeClient(this.nick, this);
                                break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                while (!Thread.currentThread().isInterrupted()) {
                    String msg;
                    msg = in.readUTF();
                    if (msg.equalsIgnoreCase("/end")) break;
                    else if (msg.startsWith("/w ")) {
                        String[] tokens = msg.split("\\s");
                        if (tokens.length > 1) {
                            String receiverNick = tokens[1];
                            StringBuilder msgBuilder = new StringBuilder(" :");
                            for (int i = 2; i < tokens.length; i++) {
                                msgBuilder.append(" ");
                                msgBuilder.append(tokens[i]);
                            }
                            if (server.isNickConnected(receiverNick)) {
                                server.sendPrivateMessage(receiverNick, "From " + this.nick + msgBuilder.toString());
                                server.sendPrivateMessage(this.nick, "To " + receiverNick + msgBuilder.toString());
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
            } finally {
                disconnect();
            }
        });
        currentThread.setDaemon(true);
        currentThread.start();
    }

    public DataOutputStream getOut() {
        return out;
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
//      server.sendPrivateMessage(this.nick, "You are disconnected.");
        this.currentThread.interrupt();
        sendMessage("/end");
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
