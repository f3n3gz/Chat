package com.geekbrains.client;

import java.io.*;
import java.util.*;


public class ChatHistory implements Serializable {
    public static ChatHistory INSTANCE = new ChatHistory();
    private static BufferedWriter bufferedWriter;
    private static RandomAccessFile randomAccessFile;


    private ChatHistory() {
    }

    public static ChatHistory getInstance() {
        return INSTANCE;
    }

    public static void openChatHistory(String login) throws IOException {

        try {
            ChatHistory.bufferedWriter = new BufferedWriter(new FileWriter(login + ".log", true));
            ChatHistory.randomAccessFile = new RandomAccessFile(login + ".log", "r");
        } catch (IOException e) {
            throw new IOException("Не могу получить доступ к файлу " + login + ".log");
        }


    }

    public static void add(String message) {
        try {
            bufferedWriter.write(message + "\n");
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> getLastMessages(int size) throws IOException {
        StringBuilder builder = new StringBuilder();
        long fileLength = randomAccessFile.length() - 1;
        ArrayList<String> messages = new ArrayList<>();
        //randomAccessFile.seek(fileLength);
        for (long i = fileLength, linesCount = 0; i >= 0 && linesCount <= size; i--) {
            randomAccessFile.seek(i);
            char c;
            c = (char) randomAccessFile.read();
            if (c == '\n') {
                linesCount++;
                messages.add(builder.reverse().toString());
                builder = new StringBuilder();
            }
            builder.append(c);
        }
        return messages;
    }

    public static void close() {
        try {
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            randomAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
