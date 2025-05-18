package Server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ObjectOutputStream objectOut;
    private ObjectInputStream objectIn;
    private List<ClientHandler> allClients;
    private String username;
    private static final String SERVER_FILES_DIR = "resources/Server/";

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.allClients = allClients;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.objectOut = new ObjectOutputStream(socket.getOutputStream());
        this.objectIn = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("LOGIN:")) {
                    String[] parts = message.substring(6).split("\\|");
                    handleLogin(parts[0], parts[1]);
                } else if (message.equals("LIST_FILES")) {
                    sendFileList();
                } else if (message.startsWith("DOWNLOAD:")) {
                    sendFile(message.substring(9));
                } else if (message.startsWith("UPLOAD:")) {
                    String[] parts = message.substring(7).split("\\|");
                    receiveFile(parts[0], Integer.parseInt(parts[1]));
                } else {
                    broadcast(username + ": " + message);
                }
            }
        } catch (Exception e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                allClients.remove(this);
                socket.close();
                System.out.println(username + " disconnected");
            } catch (IOException e) {
                System.err.println("Error closing client connection: " + e.getMessage());
            }
        }
    }

    void sendMessage(String msg) {
        out.println(msg);
    }

    private void broadcast(String msg) throws IOException {
        for (ClientHandler client : allClients) {
            if (client != this) {
                client.sendMessage(msg);
            }
        }
    }

    private void sendFileList() throws IOException {
        File serverDir = new File(SERVER_FILES_DIR);
        File[] files = serverDir.listFiles();
        StringBuilder fileList = new StringBuilder();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    if (fileList.length() > 0) fileList.append(",");
                    fileList.append(file.getName());
                }
            }
        }

        out.println("FILE_LIST:" + fileList);
    }

    private void sendFile(String fileName) throws IOException {
        Path filePath = Paths.get(SERVER_FILES_DIR + fileName);
        if (Files.exists(filePath)) {
            File file = filePath.toFile();
            objectOut.writeObject(new FileMetadata(file.getName(), file.length()));
            objectOut.write(Files.readAllBytes(filePath));
            objectOut.flush();
        } else {
            out.println("ERROR:File not found");
        }
    }

    private void receiveFile(String filename, int fileLength) throws IOException {
        byte[] fileData = new byte[fileLength];
        objectIn.readFully(fileData);
        saveUploadedFile(filename, fileData);
        out.println("UPLOAD_SUCCESS:" + filename);
    }

    private void saveUploadedFile(String filename, byte[] data) throws IOException {
        Path savePath = Paths.get(SERVER_FILES_DIR + filename);
        Files.write(savePath, data);
    }

    private void handleLogin(String username, String password) throws IOException {
        if (Server.authenticate(username, password)) {
            this.username = username;
            out.println("LOGIN_SUCCESS");
            System.out.println(username + " logged in successfully");
        } else {
            out.println("LOGIN_FAILED");
        }
    }

    // Helper class for file metadata
    private static class FileMetadata implements Serializable {
        String name;
        long size;

        FileMetadata(String name, long size) {
            this.name = name;
            this.size = size;
        }
    }
}