package com.geekbrains.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        new Thread(() -> {
            ServerSocket serv = null;
            Socket sock = null;
            try {
                serv = new ServerSocket(1000);
                System.out.println("Waiting for connection");
                sock = serv.accept();
                System.out.println("Client connected");
                DataInputStream in = new DataInputStream(sock.getInputStream());
                DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                while (true) {
                    String str = in.readUTF();
                    if (str.equals("end")) break;
                    System.out.println("Client: " + str);
                    out.writeUTF("Echo: " + str);
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    serv.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        while (true) {

        }
    }
}
