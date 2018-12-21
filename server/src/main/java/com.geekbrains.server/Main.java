package com.geekbrains.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Server server = new Server(1000);
        Thread serverThread = new Thread(server);
        serverThread.setDaemon(true);
        serverThread.start();
        while (!server.END_FLAG) {
        }
    }
}
