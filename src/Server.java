import javax.swing.*;
import java.awt.*;
import java.awt.desktop.SystemSleepEvent;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class Server extends JFrame {
    private static final int PORT = 5000;
    private static final int BUFFER_SIZE = 1024;

    private DatagramSocket socket;
    private Map<InetAddress, String> users;
    private JTextArea logArea;

    public Server() {
        users = new HashMap<>();

        setTitle("Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLayout(new BorderLayout());

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    public void start() {
        try {
            socket = new DatagramSocket(PORT);
            appendToLogArea("Server is running on port " + PORT);
            byte[] buffer = new byte[BUFFER_SIZE];

            while (true) {

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();

                String message = new String(packet.getData(), 0, packet.getLength());

                if (message.startsWith("/login")) {
                    String username = message.substring(6);
                    users.put(clientAddress, username);
                    appendToLogArea("User " + username + " logged in from " + clientAddress.getHostAddress());

                    sendToAllClients("User " + username + " joined the chat.",clientAddress,clientPort);
                } else if (message.startsWith("/chat")) {
                    String username = message.substring(5);
                    InetAddress userAddress = InetAddress.getByName(users.get(username));
                    String response = (userAddress!=null)?userAddress.getHostAddress():"User not found";


                } else {
                    String username = users.get(clientAddress);
                    String chatMessage = "[" + username + "]: " + message;
                    appendToLogArea(chatMessage);

                    sendToAllClients(chatMessage,clientAddress,clientPort);
                }

                buffer = new byte[BUFFER_SIZE];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(socket!=null){
                socket.close();
            }
        }
    }

    private void sendToAllClients(String message, InetAddress address, int port) throws Exception {
        byte[] buffer = message.getBytes();


        for (Map.Entry<InetAddress, String> entry : users.entrySet()) {
            InetAddress clientAddress = entry.getKey();

            int clientPort = PORT; // Use the server's port for all clients
            System.out.println(PORT);
            if(!clientAddress.equals(address)){
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
                socket.send(packet);
            }
        }
    }

    private void appendToLogArea(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}