package com.chatapp.server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

// Clase principal del servidor de chat
public class ChatServer {
    private static final int PORT = 12345;
    private static final CopyOnWriteArrayList<ClientHandler> clientes = new CopyOnWriteArrayList<>(); 

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) { 
            System.out.println("Servidor iniciado en el puerto " + PORT);
            
            while (true) {
                Socket socket = serverSocket.accept(); // Acepta a los clientes
                ClientHandler clientHandler = new ClientHandler(socket, clientes);
                clientes.add(clientHandler); 
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace(); // Manejo de excepciones en caso de error con el servidor
        }
    }
}
