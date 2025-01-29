package com.chatapp.server;

import java.io.IOException;

public class ProcessManager {
    public static void main(String[] args) {
        try {
            Process proceso = Runtime.getRuntime().exec("notepad.exe");
            System.out.println("Proceso iniciado: " + proceso.pid());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
