package com.chatapp.client;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
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
                enviarMensaje();
            }
        });

        entradaNuevoMensaje.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarMensaje();
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
                        reproducirSonido("src/main/java/com/chatapp/utils/sonidoNotificacion.wav");
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

    private void enviarMensaje() {
        String mensaje = entradaNuevoMensaje.getText();
        if (!mensaje.isEmpty()) {
            areaMensajes.append("Tú: " + mensaje + "\n");
            out.println(nombreUsuario + ": " + mensaje);
            entradaNuevoMensaje.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClientGUI().setVisible(true));
    }

    public static void reproducirSonido(String ruta) {
        try {
            File archivoSonido = new File(ruta);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(archivoSonido);

            // Obtener formato original
            AudioFormat formatoOriginal = audioStream.getFormat();
            AudioFormat formatoCompatible = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED, // Convertir a PCM_SIGNED
                formatoOriginal.getSampleRate(),
                16, // Convertir a 16 bits
                formatoOriginal.getChannels(),
                formatoOriginal.getChannels() * 2, 
                formatoOriginal.getSampleRate(),
                false
            );

            // Convertir el audio
            AudioInputStream audioConvertido = AudioSystem.getAudioInputStream(formatoCompatible, audioStream);

            Clip clip = AudioSystem.getClip();
            clip.open(audioConvertido);
            clip.start();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
