package com.chatapp.client;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class ChatClientGUI extends JFrame {
    // Cambiar la direccion IP por la de la computadora que funcionara como servidor
    private static final String SERVIDOR_IP = "172.25.3.68";
    private static final int SERVIDOR_PUERTO = 12345;

    private JTextPane areaMensajes;
    private JTextField entradaNuevoMensaje;
    private JButton enviarMensaje;
    private JButton botonEmoji;

    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private String nombreUsuario;

    public Boolean bandera = true;

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

        areaMensajes = new JTextPane();
        areaMensajes.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(areaMensajes);
        add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        entradaNuevoMensaje = new JTextField();
        enviarMensaje = new JButton("Enviar");
        botonEmoji = new JButton("😊");

        JButton enviarImagenBtn = new JButton("📷");

        inputPanel.add(enviarImagenBtn, BorderLayout.AFTER_LAST_LINE);
        inputPanel.add(entradaNuevoMensaje, BorderLayout.CENTER);
        inputPanel.add(enviarMensaje, BorderLayout.EAST);
        inputPanel.add(botonEmoji, BorderLayout.WEST);
        add(inputPanel, BorderLayout.SOUTH);

        enviarImagenBtn.addActionListener(e -> enviarImagen());

        JPopupMenu emojiPopup = new JPopupMenu();
        for (Emoji emoji : EmojiManager.getAll()) {
            JMenuItem emojiItem = new JMenuItem(emoji.getUnicode());
            emojiItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    entradaNuevoMensaje.setText(entradaNuevoMensaje.getText() + emoji.getUnicode());
                }
            });
            emojiPopup.add(emojiItem);
        }

        botonEmoji.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                emojiPopup.show(botonEmoji, 0, botonEmoji.getHeight());
            }
        });

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

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // Enviar el mensaje indicando que el usuario ha salido
                out.println("*** ¡" + nombreUsuario + " salió del chat! ***");

                // Cerrar el socket
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Terminar la aplicación
                System.exit(0);
            }
        });
    }

    private void conectarServidor() {
        try {
            socket = new Socket(SERVIDOR_IP, SERVIDOR_PUERTO);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(socket.getOutputStream(), true, java.nio.charset.StandardCharsets.UTF_8);

            out.println("**** ¡" + nombreUsuario + " entró al chat! ****");

            Thread receiveThread = new Thread(() -> {
                try {
                    String mensaje;
                    while ((mensaje = in.readLine()) != null) {
                        if (mensaje.startsWith("IMG:")) {
                            System.out.println("Mensaje BASE 64:" + mensaje);
                            String base64Data = mensaje.substring(4); // Extraer solo los datos Base64

                            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
                            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
                            BufferedImage image = ImageIO.read(bais);

                            // Mostrar la imagen en un JLabel
                            ImageIcon icono = new ImageIcon(image.getScaledInstance(200, 200, Image.SCALE_SMOOTH));
                            JLabel labelImagen = new JLabel(icono);
                            JOptionPane.showMessageDialog(null, labelImagen, "Imagen Recibida",
                                    JOptionPane.PLAIN_MESSAGE);
                        } else {
                            if (mensaje.startsWith("****") && mensaje.endsWith("****")) {
                                // Si el mensaje tiene los asteriscos, se considera un mensaje de ingreso
                                agregarMensaje(mensaje, "Ingresa");
                            } else if (mensaje.startsWith("***") && mensaje.endsWith("***")) {
                                agregarMensaje(mensaje, "Salida");
                            } else if (esFormatoValido(mensaje)) {
                                agregarMensaje(mensaje, "Fecha");
                            } else {
                                // Si no tiene los asteriscos, es un mensaje común
                                agregarMensaje(mensaje, "NoPropio");
                            }
                        }

                       
                        reproducirSonido("src/main/java/com/chatapp/utils/sonidoNotificacion.wav");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            "Se ha perdido la conexión con el servidor. Intentando reconectar...",
                            "Error de Conexión", JOptionPane.WARNING_MESSAGE);
                    reconnectToServer();
                }
            });
            receiveThread.start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al conectar con el servidor", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean esFormatoValido(String mensaje) {
        // Expresión regular para el formato 'yyyy-MM-dd HH:mm:ss'
        String regex = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$";
        return mensaje.matches(regex);
    }

    private void reconnectToServer() {
        new Timer(5000, e -> conectarServidor()).start();
    }

    public void enviarMensaje() {
        String mensaje = entradaNuevoMensaje.getText().trim();
        if (!mensaje.isEmpty()) {
            agregarMensaje(mensaje, "Propio"); // Mensaje alineado a la derecha
            out.println(" " + nombreUsuario + ": " + mensaje + " ");
            entradaNuevoMensaje.setText("");
            enviarHoraFecha();
        }
    }

    public void enviarImagen() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File archivoImagen = fileChooser.getSelectedFile();
            try {
                // Leer la imagen y convertirla a Base64
                FileInputStream fis = new FileInputStream(archivoImagen);
                byte[] bytes = fis.readAllBytes();
                fis.close();

                String imagenBase64 = Base64.getEncoder().encodeToString(bytes);

                // Enviar la imagen con un prefijo identificador
                out.println("IMG:" + imagenBase64);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error al enviar la imagen");
            }
        }
    }

    public void enviarHoraFecha() {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        out.println(timestamp);
    }

    public void agregarMensaje(String mensaje, String tipo) {
        StyledDocument doc = areaMensajes.getStyledDocument();
        SimpleAttributeSet estilo = new SimpleAttributeSet();

        StyleConstants.setFontSize(estilo, 13);

        switch (tipo) {
            case "Propio":
                StyleConstants.setForeground(estilo, Color.WHITE);
                StyleConstants.setBackground(estilo, Color.GREEN.darker());
                StyleConstants.setAlignment(estilo, StyleConstants.ALIGN_RIGHT);
                break;
            case "NoPropio":
                StyleConstants.setAlignment(estilo, StyleConstants.ALIGN_LEFT);
                StyleConstants.setForeground(estilo, Color.WHITE);
                StyleConstants.setBackground(estilo, Color.CYAN.darker());
                break;
            case "Ingresa":
                StyleConstants.setAlignment(estilo, StyleConstants.ALIGN_CENTER);
                StyleConstants.setForeground(estilo, Color.GREEN.darker());
                break;
            case "Salida":
                StyleConstants.setAlignment(estilo, StyleConstants.ALIGN_CENTER);
                StyleConstants.setForeground(estilo, Color.RED);
                break;
            case "Fecha":
                StyleConstants.setFontSize(estilo, 10);
                StyleConstants.setForeground(estilo, Color.GRAY);
                break;

            default:
                break;
        }

        try {
            doc.insertString(doc.getLength(), mensaje + "\n", estilo); // Insertar el mensaje
            doc.setParagraphAttributes(doc.getLength() - mensaje.length() - 1, mensaje.length() + 1, estilo, false);
        } catch (BadLocationException e) {
            e.printStackTrace();
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
                    false);

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
