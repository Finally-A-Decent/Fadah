package info.preva1l.fadah.storage.clients;

import info.preva1l.fadah.storage.protocol.response.Response;
import org.jetbrains.annotations.ApiStatus;

public interface Client extends AutoCloseable {
    @ApiStatus.Internal
    void reset();

    boolean isUsable();

    Response authorize(String username, String password);

    void close();
}
