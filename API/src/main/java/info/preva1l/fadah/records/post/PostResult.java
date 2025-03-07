package info.preva1l.fadah.records.post;

import java.util.Objects;

/**
 * Created on 7/03/2025
 *
 * @author Preva1l
 */
public class PostResult {
    public static final PostResult SUCCESS = new PostResult(0, "Success");
    public static final PostResult SUCCESS_ADVERT_FAIL = new PostResult(1, "Success (Advert Failed)");
    public static final PostResult RESTRICTED_ITEM = new PostResult(2, "Restricted Item");
    public static final PostResult MAX_LISTINGS = new PostResult(3, "Max Listings");

    private final int ordinal;
    private final String message;

    private PostResult(int ordinal, String message) {
        this.ordinal = ordinal;
        this.message = message;
    }

    public static PostResult custom(String message) {
        return new PostResult(-999, message);
    }

    public int ordinal() {
        return ordinal;
    }

    public String message() {
        return message;
    }

    public boolean successful() {
        return ordinal == PostResult.SUCCESS.ordinal() || ordinal == PostResult.SUCCESS_ADVERT_FAIL.ordinal();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostResult that = (PostResult) o;
        return ordinal == that.ordinal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ordinal);
    }
}
