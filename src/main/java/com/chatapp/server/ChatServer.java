package com.chatapp.server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

// Clase principal del servidor de chat
public class ChatServer {
    private static final int PORT = 12345; // Puerto en el que el servidor escucha conexiones
    private static final CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>(); // Lista de clientes conectados

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) { // Crea un servidor que escucha en el puerto especificado
            System.out.println("Servidor iniciado en el puerto " + PORT);
            
            while (true) {
                Socket socket = serverSocket.accept(); // Acepta nuevas conexiones de clientes
                ClientHandler clientHandler = new ClientHandler(socket, clients); // Crea un manejador para el cliente
                clients.add(clientHandler); // Agrega el cliente a la lista de clientes conectados
                new Thread(clientHandler).start(); // Inicia un hilo para manejar la comunicación con el cliente
            }
        } catch (IOException e) {
            e.printStackTrace(); // Manejo de excepciones en caso de error con el servidor
        }
    }
}
