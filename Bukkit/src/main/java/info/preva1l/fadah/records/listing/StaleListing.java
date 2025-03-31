package info.preva1l.fadah.records.listing;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

@Getter
public final class StaleListing extends BaseListing {
    private final ConcurrentSkipListSet<Bid> bids;
    private final double price;

    public StaleListing(@NotNull UUID id, @NotNull UUID owner, @NotNull String ownerName,
                         @NotNull ItemStack itemStack, @NotNull String categoryID, @NotNull String currency, double price, double tax,
                         long creationDate, long deletionDate, ConcurrentSkipListSet<Bid> bids) {
        super(id, owner, ownerName, itemStack, categoryID, currency, tax, creationDate, deletionDate);
        this.bids = bids;
        this.price = price;
    }

    @Override
    public boolean cancel(@NotNull Player canceller) {
        throw new IllegalStateException("Cancelling a listing is not possible when the listing is in a stale state!");
    }

    @Override
    public boolean canBuy(@NotNull Player player) {
        return false;
    }
}
