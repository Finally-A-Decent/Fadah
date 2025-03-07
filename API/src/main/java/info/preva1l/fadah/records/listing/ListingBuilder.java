package info.preva1l.fadah.records.listing;

import com.google.common.base.Preconditions;
import info.preva1l.fadah.currency.Currency;
import info.preva1l.fadah.currency.CurrencyRegistry;
import info.preva1l.fadah.records.post.Post;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Created on 7/03/2025
 *
 * @author Preva1l
 */
public abstract class ListingBuilder {
    protected final UUID ownerUuid;
    protected final String ownerName;
    protected ItemStack itemStack;
    protected Currency currency = CurrencyRegistry.getAll().getFirst();
    protected Double price;
    protected Double tax;
    protected Long length;
    protected boolean biddable = false;

    public ListingBuilder(Player owner) {
        this.ownerUuid = owner.getUniqueId();
        this.ownerName = owner.getName();
    }

    public ListingBuilder(UUID ownerUuid, String ownerName) {
        this.ownerUuid = ownerUuid;
        this.ownerName = ownerName;
    }

    public ListingBuilder itemStack(@NotNull ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack);
        this.itemStack = itemStack;
        return this;
    }

    public ListingBuilder currency(@NotNull Currency currency) {
        Preconditions.checkNotNull(currency);
        this.currency = currency;
        return this;
    }

    public ListingBuilder price(double price) {
        this.price = price;
        return this;
    }

    public ListingBuilder tax(double tax) {
        this.tax = tax;
        return this;
    }

    public ListingBuilder length(long length) {
        this.length = length;
        return this;
    }

    public ListingBuilder biddable(boolean biddable) {
        this.biddable = biddable;
        return this;
    }

    /**
     * Convert the listing builder into a post.
     *
     * @return the post.
     */
    public abstract Post toPost();

    /**
     * Builds the listing, if you can find a use case for this that isn't internal let me know lol
     *
     * @return a completable future of an unlisted listing object
     */
    public abstract CompletableFuture<Listing> build();
}