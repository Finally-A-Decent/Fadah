package info.preva1l.fadah.records.listing;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record Bid(
        UUID bidder,
        @SerializedName("bid_amount")
        double bidAmount,
        long timestamp
) implements Comparable<Bid> {
    @Override
    public int compareTo(@NotNull Bid o) {
        return Long.compare(o.timestamp, this.timestamp);
    }
}
