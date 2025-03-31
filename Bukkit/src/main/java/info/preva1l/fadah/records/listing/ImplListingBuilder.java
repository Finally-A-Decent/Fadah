package info.preva1l.fadah.records.listing;

import com.google.common.base.Preconditions;
import info.preva1l.fadah.cache.CategoryRegistry;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.hooks.impl.permissions.Permission;
import info.preva1l.fadah.hooks.impl.permissions.PermissionsHook;
import info.preva1l.fadah.records.post.ImplPost;
import info.preva1l.fadah.records.post.Post;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created on 7/03/2025
 *
 * @author Preva1l
 */
public final class ImplListingBuilder extends ListingBuilder {
    private final @Nullable Player player;

    public ImplListingBuilder(@NotNull Player owner) {
        super(owner);
        this.player = owner;
        this.length = Config.i().getDefaultListingLength().toMillis();
        this.tax = PermissionsHook.getValue(Double.class, Permission.LISTING_TAX, owner);
    }

    public ImplListingBuilder(UUID ownerUuid, String ownerName) {
        super(ownerUuid, ownerName);
        this.player = null;
        this.length = Config.i().getDefaultListingLength().toMillis();
    }

    @Override
    public Post toPost() {
        Preconditions.checkNotNull(itemStack);
        Preconditions.checkNotNull(currency);
        Preconditions.checkNotNull(price);
        Preconditions.checkNotNull(tax);
        Preconditions.checkNotNull(length);
        return new ImplPost(this, player);
    }

    @Override
    public CompletableFuture<Listing> build() {
        Preconditions.checkNotNull(itemStack);
        Preconditions.checkNotNull(currency);
        Preconditions.checkNotNull(price);
        Preconditions.checkNotNull(tax);
        Preconditions.checkNotNull(length);

        return CategoryRegistry.getCategoryForItem(itemStack)
                .thenApplyAsync(category -> {
                    if (biddable) {
                        return new ImplBidListing(
                                UUID.randomUUID(),
                                ownerUuid,
                                ownerName,
                                itemStack,
                                category,
                                currency.getId(),
                                price,
                                tax,
                                System.currentTimeMillis(),
                                Instant.now().plus(length, ChronoUnit.MILLIS).toEpochMilli(),
                                new ConcurrentSkipListSet<>()
                        );
                    } else {
                        return new ImplBinListing(
                                UUID.randomUUID(),
                                ownerUuid,
                                ownerName,
                                itemStack,
                                category,
                                currency.getId(),
                                price,
                                tax,
                                System.currentTimeMillis(),
                                Instant.now().plus(length, ChronoUnit.MILLIS).toEpochMilli()
                        );
                    }
                }, DatabaseManager.getInstance().getThreadPool());
    }
}