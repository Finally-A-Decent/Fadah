package info.preva1l.fadah.storage.protocol.response;

import com.google.gson.annotations.Expose;
import info.preva1l.fadah.storage.protocol.Action;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class Response {
    @Expose private final int code;
    @Expose private final String message;
    @Expose private final Action action;

    public Result getResult() {
        return Result.values()[code];
    }
}
