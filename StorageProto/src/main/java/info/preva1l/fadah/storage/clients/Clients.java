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
