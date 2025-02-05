package com.chatapp.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClientGUI extends JFrame {
    //Cambiar la direccion IP por la de la computadora que funcionara como servidor
    private static final String SERVIDOR_IP = "172.25.3.68";
    private static final int SERVIDOR_PUERTO = 12345;

    private JTextArea areaMensajes;
    private JTextField entradaNuevoMensaje;
    private JButton enviarMensaje;

    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private String nombreUsuario;

    public ChatClientGUI() {
        setTitle("Chat Cliente");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        nombreUsuario = JOptionPane.showInputDialog(this, "Ingresa tu nombre de usuario: ");
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre de usuario no válido. Agregando nombre por defecto.");
            nombreUsuario = "Pato anónimo";
        }

        areaMensajes = new JTextArea();
        areaMensajes.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(areaMensajes);
        add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        entradaNuevoMensaje = new JTextField();
        enviarMensaje = new JButton("Enviar");

        inputPanel.add(entradaNuevoMensaje, BorderLayout.CENTER);
        inputPanel.add(enviarMensaje, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        enviarMensaje.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        entradaNuevoMensaje.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        conectarServidor();
    }

    private void conectarServidor() {
        try {
            socket = new Socket(SERVIDOR_IP, SERVIDOR_PUERTO);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            Thread receiveThread = new Thread(() -> {
                try {
                    String mensaje;
                    while ((mensaje = in.readLine()) != null) {
                        areaMensajes.append(mensaje + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Se ha perdido la conexión con el servidor. Intentando reconectar...", 
                                                  "Error de Conexión", JOptionPane.WARNING_MESSAGE);
                    reconnectToServer();
                }
            });
            receiveThread.start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al conectar con el servidor", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reconnectToServer() {
        new Timer(5000, e -> conectarServidor()).start();
    }

    private void sendMessage() {
        String message = entradaNuevoMensaje.getText();
        if (!message.isEmpty()) {
            areaMensajes.append("Tú: " + message + "\n");
            out.println(nombreUsuario + ": " + message);
            entradaNuevoMensaje.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClientGUI().setVisible(true));
    }
}
