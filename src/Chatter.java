import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class Chatter extends JFrame {
    private static final int PORT = 5000;
    private static final int BUFFER_SIZE = 1024;

    private DatagramSocket socket;
    private InetAddress serverAddress;

    private String username;

    private JTextArea chatArea;
    private JTextField inputField;

    public Chatter(InetAddress serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void login() {
        JFrame loginFrame = new JFrame("Login");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(300, 150);
        loginFrame.setLayout(new FlowLayout());

        JLabel nameLabel = new JLabel("Username");
        JTextField nameField = new JTextField();
        nameField.setPreferredSize(new Dimension(200, 30));
        JButton loginButton = new JButton("Login");

        loginButton.addActionListener(result -> {

                username = nameField.getText();
                 // Add a space after "/login"

                try {
                    socket = new DatagramSocket();
                    String loginMessage = "/login " + username;
                    sendToServer(loginMessage);
                    startChatting();
                    loginFrame.dispose(); // Close the login frame after successful login
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

        });

        loginFrame.add(nameLabel);
        loginFrame.add(nameField);
        loginFrame.add(loginButton);
        loginFrame.setVisible(true);
    }

    public void startChatting() throws Exception {
        JFrame chatFrame = new JFrame();
        chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chatFrame.setSize(400, 300);
        chatFrame.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        inputField = new JTextField();
        inputField.setPreferredSize(new Dimension(200, 30));

        JButton sendButton = new JButton("Send");

        JPanel inputPanel = new JPanel(new FlowLayout());
        sendButton.addActionListener(result -> {
            String message = inputField.getText();
            try {
                String chatMessage = "[" + username + "]: " + message;
                sendToServer(chatMessage); // Send the message to the server
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            inputField.setText(""); // Clear the input field for the next message
        });

        inputPanel.add(inputField);
        inputPanel.add(sendButton);

        chatFrame.add(scrollPane, BorderLayout.CENTER);
        chatFrame.add(inputPanel, BorderLayout.PAGE_END); // Add the panel to the chat frame

        chatFrame.setVisible(true);


    }

    private void sendToServer(String message) throws Exception {
        try {
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, PORT);
            socket.send(packet);
            chatArea.append(message + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void receiveFromServer() throws Exception {
        new Thread(() -> {
            try {
                while (true) {
                    byte[] buffer = new byte[BUFFER_SIZE];

                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String message = new String(packet.getData(), 0, packet.getLength());
                    SwingUtilities.invokeLater(() -> {
                        chatArea.append(message + "\n"); // Append the received message to the chat area in the chatter frame
                        chatArea.setCaretPosition(chatArea.getDocument().getLength());
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        try {
            InetAddress serverAddress = InetAddress.getLocalHost(); // Server IP address
            Chatter chatter = new Chatter(serverAddress);
            chatter.login();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}