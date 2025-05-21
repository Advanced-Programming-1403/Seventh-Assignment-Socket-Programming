package Client;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Client {
    // TODO: Declare variables for socket input/output streams
    private static String username;
    private static final String HOST = "localhost";
    private static final int PORT = 12345;
    private static PrintWriter writer;
    private static BufferedReader reader;
    private static OutputStream outputStream;
    private static InputStream inputStream;


    public static void main(String[] args) throws Exception {

        try (Socket socket = new Socket(HOST, PORT )) {
            //✅: Use the socket input and output streams as needed

            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();


            Scanner scanner = new Scanner(System.in);


            // --- LOGIN PHASE ---
            System.out.println("===== Welcome to CS Music Room =====");


            boolean loggedIn = false;
            while (!loggedIn) {
                System.out.print("Username: ");
                username = scanner.nextLine();
                System.out.print("Password: ");
                String password = scanner.nextLine();



                sendLoginRequest(username, password);


                // ✅: Receive and check the server's login response
                // ✅: Set 'loggedIn = true' if credentials are correct; otherwise, prompt again
                String response = reader.readLine();

                if (response == null) {
                    System.out.println("Connection lost. Server closed the connection.");
                    loggedIn = false;
                }

                if ("LOGIN_SUCCESS".equals(response)) {
                    System.out.println("Logged in successfully!");
                    loggedIn =  true;

                    File userDir = new File("resources/Client/" + username);
                    if (!userDir.exists()) {
                        userDir.mkdirs();
                    }
                } else {
                    System.out.println("Login failed. Please try again.");
                    loggedIn = false;
                }
            }

            // --- ACTION MENU LOOP ---
            while (true) {
                printMenu();
                System.out.print("Enter choice: ");
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1" -> enterChat(scanner , socket);
                    case "2" -> uploadFile(scanner);
                    case "3" -> requestDownload(scanner);
                    case "0" -> {
                        System.out.println("Exiting...");
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            }

        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
    }

    private static void printMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. Enter chat box");
        System.out.println("2. Upload a file");
        System.out.println("3. Download a file");
        System.out.println("0. Exit");
    }

    private static void sendLoginRequest(String username, String password) {
        //✅: send the login request
        writer.println("LOGIN:" + username + ":" + password);
    }

    private static void enterChat(Scanner scanner , Socket socket) throws IOException {
        System.out.print("You have entered the chat ");


        //✅: Create and start ClientReceiver thread to continuously get new messages from server

        Thread receiverThread = new Thread(new ClientReceiver(socket));
        receiverThread.start();
        String message_string = "";
        while (!message_string.equalsIgnoreCase("/exit")){
            message_string = scanner.nextLine();

            if (!message_string.equalsIgnoreCase("/exit")){

                sendChatMessage(message_string);
            }
        }
    }

    private static void sendChatMessage(String message_to_send) throws IOException {
        //✅: send the chat message

        if (message_to_send.startsWith("/send ")) {
            writer.println("MESSAGE " + message_to_send.substring(6));
        } else {
            System.out.println("Use /send <message> to send a chat message.");
        }
    }

    private static void uploadFile(Scanner scanner) throws IOException {

        //✅: list all files in the resources/Client/<username> folder

        File userDir = new File("resources/Client/" + username);
        
        File[] files = userDir.listFiles((dir, name) -> new File(dir, name).isFile());

        if (!userDir.exists()) {
            userDir.mkdirs();
        }

        if (files == null || files.length == 0) {
            System.out.println("No files to upload.");
            return;
        }

        // Show available files
        System.out.println("Select a file to upload:");
        for (int i = 0; i < files.length; i++) {
            System.out.println((i + 1) + ". " + files[i].getName());
        }

        System.out.print("Enter file number: ");
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }

        if (choice < 0 || choice >= files.length) {
            System.out.println("Invalid choice.");
            return;
        }
        File selectedFile = files[choice];
        long fileSize = selectedFile.length();
        String fileName = selectedFile.getName();
        // ✅: Notify the server that a file upload is starting (e.g., send file metadata)
        writer.println("UPLOAD:" + fileName + ":" + fileSize);
        // ✅: Read the file into a byte array and send it over the socket
        try (FileInputStream fis = new FileInputStream(selectedFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            System.out.println("File uploaded successfully.");
        } catch (IOException e) {
            System.out.println("Error sending file: " + e.getMessage());
        }

    }

    private static void requestDownload(Scanner scanner) throws IOException {
        // ✅: Send a request to the server to retrieve the list of available files
        writer.println("LIST_FILES");

        String fileListLine = reader.readLine();
        if (fileListLine == null || fileListLine.trim().isEmpty()) {
            System.out.println("No files available on server.");
            return;
        }

        // ✅: Display the file names and prompt the user to select one
        String[] fileNames = fileListLine.split(",");
        System.out.println("Available files:");
        for (int i = 0; i < fileNames.length; i++) {
            System.out.println((i + 1) + ". " + fileNames[i]);
        }
        System.out.print("Enter file number to download: ");

        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }
        if (choice < 0 || choice >= fileNames.length) {
            System.out.println("Invalid choice.");
            return;
        }

        String fileName = fileNames[choice];
        writer.println("DOWNLOAD:" + fileName);


        String fileSizeLine = reader.readLine();

        if (fileSizeLine == null || !fileSizeLine.matches("\\d+")) {
            System.out.println("Failed to retrieve file size: " + fileSizeLine);
            return;
        }
        long fileSize = Long.parseLong(fileSizeLine);
        Path userDir = Paths.get("resources", "Client", username);
        Files.createDirectories(userDir);
        Path targetFile = userDir.resolve(fileName);

        // ✅: Download the selected file and save it to the user's folder in 'resources/Client/<username>'
        try (FileOutputStream fos = new FileOutputStream(targetFile.toFile())) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalRead = 0;
            while (totalRead < fileSize && (bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalRead += bytesRead;

                int percent = (int)((totalRead * 100) / fileSize);
                System.out.print("\rDownloading... " + percent + "%");
            }
            System.out.println("\nDownload complete.");
            System.out.println("File downloaded successfully to " + targetFile);
        } catch (IOException e) {
            System.out.println("Error receiving file: " + e.getMessage());
        }
    }


}
