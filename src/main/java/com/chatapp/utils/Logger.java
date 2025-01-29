package com.chatapp.utils;

import java.io.FileWriter;
import java.io.IOException;

public class Logger {
    public static void log(String message) {
        try (FileWriter writer = new FileWriter("chatlog.txt", true)) {
            writer.write(message + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
