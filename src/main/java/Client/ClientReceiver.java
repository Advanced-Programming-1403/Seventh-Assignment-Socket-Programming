package Client;


import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class ClientReceiver implements Runnable {
    // ✅: Declare a variable to hold the input stream from the socket
    private BufferedReader reader;
    private final List<String> chatHistory = new ArrayList<>();
    private final String historyFile = "resources/Client/chat_history.txt";

    public ClientReceiver(Socket socket) {
        // ✅: Modify this constructor to receive either a Socket or an InputStream as a parameter
        // ✅: Initialize the input stream variable using the received parameter
        try {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.out.println("Error initializing reader: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            //✅: Listen for new messages from server
            String line;
            //✅: print the  new message in CLI
            while ((line = reader.readLine()) != null) {
                chatHistory.add(line);
                System.out.println("\n[Server]: " + line);
                System.out.print("> ");
            }




        } catch (Exception e) {
            System.out.println("Disconnected from server: " + e.getMessage());
        } finally {
            saveChatHistory();
        }

    }
    // Bonus Tasks 🌟

    private void saveChatHistory() {
        try {
            Path path = Paths.get(historyFile);
            Files.createDirectories(path.getParent());
            Files.write(path, chatHistory, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            System.out.println("\nChat history saved.");
        } catch (IOException e) {
            System.out.println("Failed to save chat history: " + e.getMessage());
        }
    }

}
