package com.chatapp.server;

import java.io.*;
import java.net.*;
import java.util.List;

// Clase que maneja la comunicación con un cliente en un hilo separado
public class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private List<ClientHandler> clientes;

    public ClientHandler(Socket socket, List<ClientHandler> clientes) {
        this.socket = socket;
        this.clientes = clientes;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String mensaje;
            while ((mensaje = in.readLine()) != null) {
                System.out.println("Mensaje recibido de " + mensaje);
                for (ClientHandler cliente : clientes) {
                    if (cliente != this) {
                        if (mensaje.startsWith("NOTIF_IMG:")) {
                            String nuevoMensaje = mensaje.substring(10);
                            cliente.enviarMensaje(nuevoMensaje);
                        } else {
                            cliente.enviarMensaje(mensaje);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println(this + " desconectado.");
            enviarMensaje(" desconectado.");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void enviarMensaje(String message) {
        out.println(message);
    }
}
