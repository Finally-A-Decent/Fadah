package info.preva1l.fadah.records.listing;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.SortedSet;
import java.util.UUID;

public final class BidListing extends ActiveListing {
    public BidListing(@NotNull UUID id, @NotNull UUID owner, @NotNull String ownerName,
                      @NotNull ItemStack itemStack, @NotNull String categoryID, @NotNull String currency, double price,
                      double tax, long creationDate, long deletionDate, SortedSet<Bid> bids) {
        super(id, owner, ownerName, itemStack, categoryID, currency, price, tax, creationDate, deletionDate, bids);
    }

    @Override
    public void purchase(@NotNull Player buyer) {
        throw new IllegalStateException("Tried to add Buy a Bidding auction!");
    }

    /**
     * Add a new bid
     *
     * @param bidder    the person bidding
     * @param bidAmount the amount of the bid
     * @return true if the bid was successful, false if the bid is not high enough
     */
    @Override
    public boolean newBid(@NotNull Player bidder, double bidAmount) {
        return false;
    }
}
