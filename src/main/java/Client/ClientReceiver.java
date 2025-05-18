package Client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientReceiver implements Runnable {
    private BufferedReader in;

    public ClientReceiver(Socket socket) {
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            System.err.println("Error creating input stream: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(message);
            }
        } catch (Exception e) {
            System.err.println("Connection lost: " + e.getMessage());
        } finally {
            try {
                if (in != null) in.close();
            } catch (Exception e) {
                System.err.println("Error closing stream: " + e.getMessage());
            }
        }
    }
}