package info.preva1l.fadah.storage.exceptions;

public class ConnectionFailedException extends RuntimeException {
    public ConnectionFailedException(String message, Throwable reason) {
        super(message, reason);
    }
}
