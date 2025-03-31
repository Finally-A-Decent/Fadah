package info.preva1l.fadah.records.listing;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.api.ListingEndEvent;
import info.preva1l.fadah.api.ListingEndReason;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.records.collection.CollectableItem;
import info.preva1l.fadah.records.collection.ExpiredItems;
import info.preva1l.fadah.utils.TaskManager;
import info.preva1l.fadah.utils.logging.TransactionLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class ActiveListing extends BaseListing {
    protected ActiveListing(@NotNull UUID id, @NotNull UUID owner, @NotNull String ownerName,
                            @NotNull ItemStack itemStack, @NotNull String categoryID, @NotNull String currency,
                            double tax, long creationDate, long deletionDate) {
        super(id, owner, ownerName, itemStack, categoryID, currency, tax, creationDate, deletionDate);
    }

    @Override
    public boolean cancel(@NotNull Player canceller) {
        if (CacheAccess.get(Listing.class, this.getId()).isEmpty()) {
            Lang.sendMessage(canceller, Lang.i().getPrefix() + Lang.i().getErrors().getDoesNotExist());
            return false;
        }
        Lang.sendMessage(canceller, Lang.i().getPrefix() + Lang.i().getNotifications().getCancelled());
        CacheAccess.invalidate(Listing.class, this);
        DatabaseManager.getInstance().delete(Listing.class, this);

        CacheAccess.getNotNull(ExpiredItems.class, getOwner()).add(CollectableItem.of(itemStack));

        boolean isAdmin = !this.isOwner(canceller);
        TransactionLogger.listingRemoval(this, isAdmin);
        TaskManager.Sync.run(Fadah.getInstance(), () ->
                Bukkit.getServer().getPluginManager().callEvent(
                        new ListingEndEvent(this, isAdmin
                                ? ListingEndReason.CANCELLED_ADMIN
                                : ListingEndReason.CANCELLED)
                )
        );
        return true;
    }

    @Override
    public boolean canBuy(@NotNull Player player) {
        if (isOwner(player)) {
            Lang.sendMessage(player, Lang.i().getPrefix() + Lang.i().getErrors().getOwnListings());
            return false;
        }

        if (CacheAccess.get(Listing.class, getId()).isEmpty()) {
            Lang.sendMessage(player, Lang.i().getPrefix() + Lang.i().getErrors().getDoesNotExist());
            return false;
        }

        if (System.currentTimeMillis() <= getDeletionDate()) {
            Lang.sendMessage(player,  Lang.i().getPrefix() + Lang.i().getErrors().getDoesNotExist());
            return false;
        }

        return true;
    }

    public abstract StaleListing getAsStale();
}
