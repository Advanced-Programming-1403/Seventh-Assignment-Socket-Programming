package Client;

import com.google.gson.Gson;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Client {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 12345;
    private static String username;
    private static Socket socket;
    private static ObjectOutputStream objectOut;
    private static ObjectInputStream objectIn;
    private static PrintWriter jsonOut;
    private static BufferedReader jsonIn;
    private static Gson gson = new Gson();

    public static void main(String[] args) {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            objectOut = new ObjectOutputStream(socket.getOutputStream());
            objectIn = new ObjectInputStream(socket.getInputStream());
            jsonOut = new PrintWriter(socket.getOutputStream(), true);
            jsonIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Scanner scanner = new Scanner(System.in);
            System.out.println("===== Welcome to CS Music Room =====");

            // Login Phase
            boolean loggedIn = false;
            while (!loggedIn) {
                System.out.print("Username: ");
                username = scanner.nextLine();
                System.out.print("Password: ");
                String password = scanner.nextLine();
                loggedIn = sendLoginRequest(username, password);
                if (!loggedIn) System.out.println("Invalid credentials. Try again.");
            }

            // Main Menu
            while (true) {
                printMenu();
                System.out.print("Enter choice: ");
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1" -> enterChat(scanner);
                    case "2" -> uploadFile(scanner);
                    case "3" -> requestDownload(scanner);
                    case "0" -> {
                        System.out.println("Exiting...");
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Connection error: " + e.getMessage());
        } finally {
            try { if (socket != null) socket.close(); }
            catch (IOException e) { e.printStackTrace(); }
        }
    }

    private static boolean sendLoginRequest(String username, String password) throws IOException {
        jsonOut.println(gson.toJson(new LoginRequest(username, password)));
        String response = jsonIn.readLine();
        return response != null && response.equals("LOGIN_SUCCESS");
    }

    private static void enterChat(Scanner scanner) throws IOException {
        System.out.println("Entered chat. Type '/exit' to leave.");
        new Thread(new ClientReceiver()).start();

        String message;
        while (!(message = scanner.nextLine()).equalsIgnoreCase("/exit")) {
            jsonOut.println(gson.toJson(new ChatMessage(username, message)));
        }
    }

    private static void uploadFile(Scanner scanner) throws IOException {
        Path userDir = Paths.get("resources/Client/" + username);
        File[] files = userDir.toFile().listFiles();

        if (files == null || files.length == 0) {
            System.out.println("No files to upload.");
            return;
        }

        System.out.println("Select a file to upload:");
        for (int i = 0; i < files.length; i++) {
            System.out.println((i + 1) + ". " + files[i].getName());
        }

        int choice = Integer.parseInt(scanner.nextLine()) - 1;
        if (choice < 0 || choice >= files.length) {
            System.out.println("Invalid choice.");
            return;
        }

        File file = files[choice];
        objectOut.writeObject(new FileMetadata(file.getName(), file.length()));
        objectOut.write(Files.readAllBytes(file.toPath()));
        System.out.println("File uploaded: " + file.getName());
    }

    private static void requestDownload(Scanner scanner) throws IOException, ClassNotFoundException {
        jsonOut.println("LIST_FILES");
        String[] files = gson.fromJson(jsonIn.readLine(), String[].class);

        if (files.length == 0) {
            System.out.println("No files available.");
            return;
        }

        System.out.println("Available files:");
        for (int i = 0; i < files.length; i++) {
            System.out.println((i + 1) + ". " + files[i]);
        }

        int choice = Integer.parseInt(scanner.nextLine()) - 1;
        if (choice < 0 || choice >= files.length) {
            System.out.println("Invalid choice.");
            return;
        }

        jsonOut.println(gson.toJson(new FileRequest(files[choice])));
        FileMetadata metadata = (FileMetadata) objectIn.readObject();
        byte[] fileData = new byte[(int) metadata.getSize()];
        objectIn.readFully(fileData);

        Path savePath = Paths.get("resources/Client/" + username + "/" + metadata.getName());
        Files.write(savePath, fileData);
        System.out.println("File downloaded: " + metadata.getName());
    }

    private static void printMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. Enter chat box");
        System.out.println("2. Upload a file");
        System.out.println("3. Download a file");
        System.out.println("0. Exit");
    }

    // Inner Classes for Message Structure
    private static class LoginRequest {
        String username;
        String password;
        LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    private static class ChatMessage {
        String sender;
        String content;
        ChatMessage(String sender, String content) {
            this.sender = sender;
            this.content = content;
        }
    }

    private static class FileMetadata implements Serializable {
        String name;
        long size;
        FileMetadata(String name, long size) {
            this.name = name;
            this.size = size;
        }
        long getSize() { return size; }
        String getName() { return name; }
    }

    private static class FileRequest {
        String filename;
        FileRequest(String filename) {
            this.filename = filename;
        }
    }

    // Thread to Receive Messages
    private static class ClientReceiver implements Runnable {
        public void run() {
            try {
                String message;
                while ((message = jsonIn.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                System.out.println("Disconnected from chat.");
            }
        }
    }
}