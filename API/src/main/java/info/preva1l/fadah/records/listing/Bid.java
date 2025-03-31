package info.preva1l.fadah.records.listing;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * A bid on a listing.
 * <br><br>
 * Created on 24/06/2024
 *
 * @author Preva1l
 * @param bidder the player who made the bid.
 * @param bidAmount the amount of the bid.
 * @param timestamp the epoch time the bid was placed.
 */
public record Bid(
        UUID bidder,
        String bidderName,
        @SerializedName("bid_amount")
        double bidAmount,
        long timestamp
) implements Comparable<Bid> {
    @Override
    public int compareTo(@NotNull Bid o) {
        return Long.compare(o.timestamp, this.timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bid bid = (Bid) o;
        return bidder.equals(bid.bidder) && timestamp == bid.timestamp;
    }
}
