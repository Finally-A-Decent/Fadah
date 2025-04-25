package info.preva1l.fadah.records.history;

import com.google.gson.annotations.Expose;
import info.preva1l.fadah.api.AuctionHouseAPI;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * A historic item.
 * <br><br>
 * Created on 13/04/2024
 *
 * @param loggedDate when the action happened, in epoch millis.
 * @param action     the action that got logged.
 * @param itemStack  the item that had an action performed on it.
 * @param price      nullable, only used for {@link LoggedAction#LISTING_START}, {@link LoggedAction#LISTING_PURCHASED}, {@link LoggedAction#LISTING_SOLD}
 * @param playerUUID nullable, only used for {@link LoggedAction#LISTING_SOLD} and {@link LoggedAction#LISTING_PURCHASED}
 * @author Preva1l
 */
public record HistoricItem(
        @Expose @NotNull Long loggedDate,
        @Expose @NotNull LoggedAction action,
        @Expose @NotNull ItemStack itemStack,
        @Expose @Nullable Double price,
        @Expose @Nullable UUID playerUUID,
        @Expose @Nullable Boolean biddable
) implements Comparable<HistoricItem> {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HistoricItem(
                Long date, LoggedAction action1, ItemStack stack, Double price1, UUID buyerUid
                , Boolean biddable1
        ))) return false;
        return Objects.equals(price, price1)
                && Objects.equals(loggedDate, date)
                && Objects.equals(playerUUID, buyerUid)
                && action == action1
                && Objects.equals(itemStack, stack)
                && Objects.equals(biddable, biddable1);
    }

    /**
     * An enum of the possible actions to get logged.
     */
    public enum LoggedAction {
        /**
         * When the player starts a listing.
         */
        LISTING_START,
        /**
         * When the player purchases a listing.
         */
        LISTING_PURCHASED,
        /**
         * When a player sells a listing.
         */
        LISTING_SOLD,
        /**
         * When a player cancels a listing.
         */
        LISTING_CANCEL,
        /**
         * When a player's listing expired.
         */
        LISTING_EXPIRE,
        /**
         * When an admin cancels a player's listing.
         */
        LISTING_ADMIN_CANCEL,
        /**
         * When a player claims an expired item.
         */
        EXPIRED_ITEM_CLAIM,
        /**
         * When an admin claims a player's expired item.
         */
        EXPIRED_ITEM_ADMIN_CLAIM,
        /**
         * When a player claims an item in their collection box.
         */
        COLLECTION_BOX_CLAIM,
        /**
         * When an admin claims an item in a player's collection box.
         */
        COLLECTION_BOX_ADMIN_CLAIM;

        /**
         * Get the actions locale value from the plugins config file.
         *
         * @return the raw locale string.
         */
        public String getLocaleActionName() {
            return AuctionHouseAPI.getInstance().getLoggedActionLocale(this);
        }
    }

    @Override
    public int compareTo(@NotNull HistoricItem o) {
        return Long.compare(o.loggedDate, this.loggedDate);
    }
}
