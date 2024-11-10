package info.preva1l.fadah;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class StorageService {
    private static final int PORT = 5317;

    public static void main(String[] args) {
        new StorageService().start();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Storage server running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}