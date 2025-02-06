package com.chatapp.server;

import java.io.*;
import java.net.*;
import java.util.List;

import com.chatapp.client.ChatClientGUI;

// Clase que maneja la comunicación con un cliente en un hilo separado
public class ClientHandler implements Runnable {
    private Socket socket; // Socket para la conexión con el cliente
    private PrintWriter out; // Objeto para enviar mensajes al cliente
    private BufferedReader in; // Objeto para recibir mensajes del cliente
    private List<ClientHandler> clientes; // Lista de clientes conectados al servidor

    // Constructor que recibe el socket del cliente y la lista de clientes conectados
    public ClientHandler(Socket socket, List<ClientHandler> clientes) {
        this.socket = socket;
        this.clientes = clientes;
    }

    @Override
    public void run() {
        try {
            // Inicializa los flujos de entrada y salida
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String mensaje;
            // Bucle que recibe mensajes del cliente
            while ((mensaje = in.readLine()) != null) {
                System.out.println("Mensaje recibido de " + mensaje);
                // Envía el mensaje a todos los clientes conectados excepto al remitente
                for (ClientHandler cliente : clientes) {
                    if (cliente != this) {
                        if(mensaje.startsWith("NOTIF_IMG:")){
                            String nuevoMensaje = mensaje.substring(10);
                            cliente.enviarMensaje(nuevoMensaje);
                        } else {
                            cliente.enviarMensaje(mensaje);
                        }                   
                    } 
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Manejo de errores de entrada/salida
        } finally {
            System.out.println(this + " desconectado.");
            enviarMensaje(" desconectado.");
            try { socket.close(); } catch (IOException e) { e.printStackTrace(); } // Cierra el socket cuando el cliente se desconecta
        }
    }

    // Método para enviar mensajes a un cliente
    public void enviarMensaje(String message) {
        out.println(message);
    }
}
