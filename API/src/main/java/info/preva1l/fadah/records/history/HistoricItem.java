package info.preva1l.fadah.records.history;

import com.google.gson.annotations.Expose;
import info.preva1l.fadah.api.AuctionHouseAPI;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * A historic item
 *
 * @param ownerUUID     the person who the log belongs to
 * @param loggedDate    when the action happened, in epoch millis
 * @param action        the action that got logged
 * @param itemStack     the item that had an action happen to it
 * @param price         Nullable, only used for {@link LoggedAction#LISTING_START}, {@link LoggedAction#LISTING_PURCHASED}, {@link LoggedAction#LISTING_SOLD}
 * @param purchaserUUID Nullable, only used for {@link LoggedAction#LISTING_SOLD}
 */
public record HistoricItem(
        @Expose @NotNull UUID ownerUUID,
        @Expose @NotNull Long loggedDate,
        @Expose @NotNull LoggedAction action,
        @Expose @NotNull ItemStack itemStack,
        @Expose @Nullable Double price,
        @Expose @Nullable UUID purchaserUUID
) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HistoricItem(
                UUID ownerUid, Long date, LoggedAction action1, ItemStack stack, Double price1, UUID buyerUid
        ))) return false;
        return Objects.equals(price, price1)
                && Objects.equals(ownerUUID, ownerUid)
                && Objects.equals(loggedDate, date)
                && Objects.equals(purchaserUUID, buyerUid)
                && action == action1
                && Objects.equals(itemStack, stack);
    }

    public enum LoggedAction {
        LISTING_START,
        LISTING_PURCHASED,
        LISTING_SOLD,
        LISTING_CANCEL,
        LISTING_EXPIRE,
        LISTING_ADMIN_CANCEL,
        EXPIRED_ITEM_CLAIM,
        EXPIRED_ITEM_ADMIN_CLAIM,
        COLLECTION_BOX_CLAIM,
        COLLECTION_BOX_ADMIN_CLAIM;

        public String getLocaleActionName() {
            return AuctionHouseAPI.getInstance().getLoggedActionLocale(this);
        }
    }
}
