package info.preva1l.fadah.storage.exceptions;

import info.preva1l.fadah.storage.protocol.response.Response;

public class AuthorizationFailedException extends RuntimeException {
    public AuthorizationFailedException(Response response) {
        super(response.getMessage());
    }
}
