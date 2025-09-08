package info.preva1l.fadah.records.listing;

import info.preva1l.fadah.currency.Currency;
import info.preva1l.fadah.currency.CurrencyRegistry;
import info.preva1l.fadah.records.Category;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A listing that gets posted to the auction house.
 * <br><br>
 * Created on 13/04/2024
 *
 * @author Preva1l
 */
public interface Listing {
    /**
     * A unique identifier to identify this listing.
     * @return the uuid.
     */
    @NotNull UUID getId();

    /**
     * The listing owners UUID.
     * @return the listing owners uuid.
     */
    @NotNull UUID getOwner();

    /**
     * The listing owners name.
     * @return the owners name.
     */
    @NotNull String getOwnerName();

    /**
     * The itemstack of the listing.
     * @return the item.
     */
    @NotNull ItemStack getItemStack();

    /**
     * The id of the category the listing falls into.
     * @return the category id.
     */
    @NotNull String getCategoryID();

    /**
     * The id of the currency used.
     * @return the currency id.
     */
    @NotNull String getCurrencyId();

    /**
     * The % of tax that will be charged on this listing.
     * @return the tax.
     */
    double getTax();

    /**
     * The epoch time stamp the listing was created.
     * @return the creation date.
     */
    long getCreationDate();

    /**
     * The epoch time stamp the listing will expire/complete.
     * @return the deletion date.
     */
    long getDeletionDate();

    /**
     * The price or the top bid of the listing
     *
     * @return the price.
     */
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
     * Get the category that this listing should be listed under.
     *
     * @return the category for this listing.
     */
    Category getCategory();

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
     */
    CompletableFuture<Void> cancel(@NotNull Player canceller);

    /**
     * Check if a player can buy the listing, or place a bid.
     *
     * @param player the player to check.
     * @return true if the player can purchase the listing, else false.
     */
    boolean canBuy(@NotNull Player player);

    /**
     * Check if the bidding is still active.
     *
     * @return true if the bidding is still active.
     */
    boolean isActive();

    /**
     * Expire the listing.
     */
     default CompletableFuture<Void> expire() {
        return expire(false);
    }

    /**
     * Expire the listing.
     *
     * @param force if false, this method will only run if the listing is meant to be expired
     */
    CompletableFuture<Void> expire(boolean force);
}