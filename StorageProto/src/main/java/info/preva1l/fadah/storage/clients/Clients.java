package info.preva1l.fadah.storage.clients;

import info.preva1l.fadah.storage.clients.pooled.ClientPool;
import info.preva1l.fadah.storage.exceptions.AuthorizationFailedException;
import info.preva1l.fadah.storage.protocol.response.Response;
import info.preva1l.fadah.storage.protocol.response.Result;
import lombok.experimental.UtilityClass;

/**
 * Clients factory to create a connection to the storage service.
 */
@UtilityClass
public class Clients {
    /**
     * {@code fadah://username:password@hostname:port}
     */
    public ClientPool createPooled(String uri) {
        if (!uri.startsWith("fadah://")) throw new RuntimeException("URI is not formatted to be a fadah connection!");
        uri = uri.replace("fadah://", "");
        String[] parts = uri.split("@");
        String[] creds = parts[0].split(":");
        String[] conn = parts[1].split(":");
        return createPooled(conn[0], Integer.parseInt(conn[1]), creds[0], creds[1]);
    }

    public ClientPool createPooled(String host, int port, String username, String password) {
        return createPooled(host, port, username, password, 10);
    }

    public ClientPool createPooled(String host, int port, String username, String password, int poolSize) {
        return new ClientPool(host, port, poolSize);
    }

    public Client create(String host, int port, String username, String password) {
        Client client = new SyncClient(host, port);
        Response response = client.authorize(username, password);
        if (response.getResult() == Result.FAIL) {
            throw new AuthorizationFailedException(response);
        }
        return client;
    }
}
