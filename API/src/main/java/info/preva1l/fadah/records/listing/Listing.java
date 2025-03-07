package info.preva1l.fadah.records.listing;

import info.preva1l.fadah.currency.Currency;
import info.preva1l.fadah.currency.CurrencyRegistry;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.SortedSet;
import java.util.UUID;

@Getter
public abstract class Listing {
    final @NotNull UUID id;
    final @NotNull UUID owner;
    final @NotNull String ownerName;
    final @NotNull ItemStack itemStack;
    final @NotNull String categoryID;
    final @NotNull String currencyId;
    final double price;
    final double tax;
    final long creationDate;
    final long deletionDate;
    final SortedSet<Bid> bids;

    protected Listing(@NotNull UUID id, @NotNull UUID owner, @NotNull String ownerName,
                      @NotNull ItemStack itemStack, @NotNull String categoryID, @NotNull String currency, double price,
                      double tax, long creationDate, long deletionDate, SortedSet<Bid> bids) {
        this.id = id;
        this.owner = owner;
        this.ownerName = ownerName;
        this.itemStack = itemStack;
        this.categoryID = categoryID;
        this.currencyId = currency;
        this.price = price;
        this.tax = tax;
        this.creationDate = creationDate;
        this.deletionDate = deletionDate;
        this.bids = bids;
    }

    public Currency getCurrency() {
        return CurrencyRegistry.get(currencyId);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isOwner(@NotNull Player player) {
        return player.getUniqueId().equals(this.owner);
    }

    public boolean isOwner(@NotNull UUID uuid) {
        return this.owner.equals(uuid);
    }

    public abstract void purchase(@NotNull Player buyer);

    /**
     * Add a new bid
     * @param bidder the person bidding
     * @param bidAmount the amount of the bid
     * @return true if the bid was successful, false if the bid is not high enough
     */
    public abstract boolean newBid(@NotNull Player bidder, double bidAmount);

    public abstract boolean cancel(@NotNull Player canceller);

    public abstract boolean canBuy(@NotNull Player player);
}