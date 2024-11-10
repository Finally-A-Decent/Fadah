package info.preva1l.fadah.storage.clients;

import info.preva1l.fadah.storage.clients.pooled.ClientPool;
import info.preva1l.fadah.storage.exceptions.ConnectionFailedException;
import info.preva1l.fadah.storage.protocol.response.Response;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SyncClient implements Client {
    private Socket socket;
    private final String host;
    private final int port;
    private Thread thread;
    private @Nullable ClientPool parent;


    public SyncClient(String host, int port) {
        this.host = host;
        this.port = port;
        initialize();
    }

    public SyncClient(String host, int port, @Nullable ClientPool parent) {
        this.host = host;
        this.port = port;
        this.parent = parent;
        initialize();
    }

    private void initialize() {
        String number = parent == null ? "01" : (parent.getSize() >= 10 ? "" + parent.getSize() : "0" + parent.getSize());
        this.thread = new Thread(this::poll, "StorageServiceClient-" + number);
        thread.setDaemon(true);
        try {
            this.socket = new Socket(host, port);
        } catch (IOException e) {
            throw new ConnectionFailedException("Socket failed to initiate", e);
        }
    }

    private void poll() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String message;
            while ((message = in.readLine()) != null) {
                // todo: handle
            }

            socket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error while polling messages", e);
        }
    }

    @Override
    public void reset() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            initialize();
        } catch (IOException e) {
            throw new RuntimeException("Error while resetting client", e);
        }
    }

    @Override
    public Response authorize(String username, String password) {
        return null;
    }

    @Override
    public boolean isUsable() {
        return socket != null && !socket.isClosed() && thread.isAlive();
    }

    @Override
    public void close() {
        if (parent != null) {
            parent.releaseConnection(this);
            return;
        }
    }
}
