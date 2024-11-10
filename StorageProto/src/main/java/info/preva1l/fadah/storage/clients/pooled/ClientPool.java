package info.preva1l.fadah.storage.clients.pooled;

import info.preva1l.fadah.storage.clients.Client;
import info.preva1l.fadah.storage.clients.SyncClient;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientPool {
    private final BlockingQueue<Client> pool;
    private final String host;
    private final int port;

    public ClientPool(String host, int port, int poolSize) {
        this.host = host;
        this.port = port;
        this.pool = new LinkedBlockingQueue<>(poolSize);

        for (int i = 0; i < poolSize; i++) {
            pool.offer(new SyncClient(host, port));
        }
    }

    public int getSize() {
        return pool.size();
    }

    public Client getConnection() throws InterruptedException {
        return pool.take();
    }

    public void releaseConnection(Client client) {
        if (client.isUsable()) {
            pool.offer(client);
        } else {
            pool.offer(new SyncClient(host, port));
        }
    }

    public void close() {
        pool.forEach(Client::close);
    }
}