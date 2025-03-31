package info.preva1l.fadah.records.listing;

import info.preva1l.fadah.currency.Currency;
import info.preva1l.fadah.currency.CurrencyRegistry;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * A listing that gets posted to the auction house.
 * <br><br>
 * Created on 13/04/2024
 *
 * @author Preva1l
 */
public interface Listing {
    @NotNull UUID getId();
    @NotNull UUID getOwner();
    @NotNull String getOwnerName();
    @NotNull ItemStack getItemStack();
    @NotNull String getCategoryID();
    @NotNull String getCurrencyId();
    double getTax();
    long getCreationDate();
    long getDeletionDate();

    double getPrice();

    /**
     * Get the currency the listing was posted with.
     *
     * @return the currency used to list the item.
     */
    default Currency getCurrency() {
        return CurrencyRegistry.get(getCurrencyId());
    }

    /**
     * Check if a player owns the listing.
     *
     * @param player the player to check.
     * @return true if the player is the owner.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    default boolean isOwner(@NotNull Player player) {
        return player.getUniqueId().equals(this.getOwner());
    }

    /**
     * Check if a player owns the listing.
     *
     * @param uuid the player's uuid to check.
     * @return true if the player is the owner.
     */
    default boolean isOwner(@NotNull UUID uuid) {
        return this.getOwner().equals(uuid);
    }

    /**
     * Cancels the listing if it is active.
     *
     * @param canceller the player attempting to cancel the listing.
     * @return true if it was cancelled, false if the player does not own the listing or have management permissions.
     */
    boolean cancel(@NotNull Player canceller);

    /**
     * Check if a player can buy the listing, or place a bid.
     *
     * @param player the player to check.
     * @return true if the player can purchase the listing, else false.
     */
    boolean canBuy(@NotNull Player player);
}