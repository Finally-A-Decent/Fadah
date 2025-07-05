package info.preva1l.fadah.records.listing;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created on 5/07/2025
 *
 * @author Preva1l
 */
public final class ListingFactory {
    public static Listing create(
            boolean biddable,
            @NotNull UUID id,
            @NotNull UUID ownerUUID,
            @NotNull String ownerName,
            @NotNull ItemStack itemStack,
            @NotNull String currency,
            double price,
            double tax,
            long creationDate,
            long deletionDate,
            @Nullable ConcurrentSkipListSet<Bid> bids
    ) {
        if (biddable) {
            return new ImplBidListing(
                    id,
                    ownerUUID,
                    ownerName,
                    itemStack,
                    currency,
                    price,
                    tax,
                    creationDate,
                    deletionDate,
                    bids
            );
        } else {
            return new ImplBinListing(
                    id,
                    ownerUUID,
                    ownerName,
                    itemStack,
                    currency,
                    price,
                    tax,
                    creationDate,
                    deletionDate
            );
        }
    }
}
