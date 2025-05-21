package Server;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import Client.Client;
import Shared.User;
public class Server {
    // Predefined users for authentication
    private static final User[] users = {
            new User("user1", "1234"),
            new User("user2", "1234"),
            new User("user3", "1234"),
            new User("user4", "1234"),
            new User("user5", "1234"),
    };

    // List of currently connected clients
    public static ArrayList<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        final int PORT = 12345;
        // ✅: Create a ServerSocket listening on a port (e.g., 12345)
        ServerSocket serverSocket = new ServerSocket(PORT);


        // ✅: Accept incoming client connections in a loop
        //       For each connection:
        //       - Create a new ClientHandler object
        //       - Add it to the 'clients' list
        //       - Start a new thread to handle communication

        while (true){
            Socket clientSokect = serverSocket.accept();
            System.out.println("New user connected : " + clientSokect.getInetAddress());

            ClientHandler handler = new ClientHandler(clientSokect , clients);
            clients.add(handler);
            new Thread(handler).start();

        }
    }

    public static boolean authenticate(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }
}
