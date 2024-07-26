package info.preva1l.fadah.multiserver;

import com.google.gson.Gson;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CollectionBoxCache;
import info.preva1l.fadah.cache.ExpiredListingsCache;
import info.preva1l.fadah.cache.HistoricItemsCache;
import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.guis.FastInvManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class Broker {
    protected final Fadah plugin;
    protected final Gson gson;

    protected Broker(@NotNull Fadah plugin) {
        this.plugin = plugin;
        this.gson = new Gson();
    }

    protected void handle(@NotNull Message message) {
        switch (message.getType()) {
            case LISTING_ADD -> message.getPayload()
                    .getUUID().ifPresentOrElse(uuid -> {
                        Fadah.getINSTANCE().getDatabase().getListing(uuid)
                                .thenAccept(ListingCache::addListing);
                        }, () -> {
                        throw new IllegalStateException("Listing add message received with no listing UUID!");
                    });

            case LISTING_REMOVE -> message.getPayload()
                    .getUUID().ifPresentOrElse(uuid -> {
                        Listing listing = ListingCache.getListing(uuid);
                        if (listing == null) {
                            throw new IllegalStateException("Listing remove message received, but we do not have the same listing?");
                        }
                        ListingCache.removeListing(listing);
                        }, () -> {
                        throw new IllegalStateException("Listing remove message received with no listing UUID!");
                    });

            case COLLECTION_BOX_UPDATE -> message.getPayload()
                    .getUUID().ifPresentOrElse(uuid -> {
                        Fadah.getINSTANCE().getDatabase().getCollectionBox(uuid)
                                .thenAccept(items -> CollectionBoxCache.update(uuid, items));
                        }, () -> {
                        throw new IllegalStateException("Collection box update message received with no player UUID!");
                    });

            case EXPIRED_LISTINGS_UPDATE -> message.getPayload()
                    .getUUID().ifPresentOrElse(uuid -> {
                        Fadah.getINSTANCE().getDatabase().getExpiredItems(uuid)
                                .thenAccept(items -> ExpiredListingsCache.update(uuid, items));
                        }, () -> {
                        throw new IllegalStateException("Expired listings update message received with no player UUID!");
                    });

            case HISTORY_UPDATE -> message.getPayload()
                    .getUUID().ifPresentOrElse(uuid -> {
                        Fadah.getINSTANCE().getDatabase().getHistory(uuid)
                                .thenAccept(items -> HistoricItemsCache.update(uuid, items));
                        }, () -> {
                        throw new IllegalStateException("History update message received with no player UUID!");
                    });

            case NOTIFICATION -> message.getPayload()
                    .getNotification().ifPresentOrElse(notification -> {
                        Player player = Bukkit.getPlayer(notification.getPlayer());
                        if (player == null) return;

                        player.sendMessage(StringUtils.colorize(notification.getMessage()));
                        }, () -> {
                        throw new IllegalStateException("Notification message received with no notification info!");
                    });

            case RELOAD -> {
                Fadah.getINSTANCE().reload();
                Bukkit.getConsoleSender().sendMessage(Lang.PREFIX.toFormattedString() + Lang.ADMIN_RELOAD_REMOTE.toFormattedString());
            }

            case TOGGLE -> {
                FastInvManager.closeAll(Fadah.getINSTANCE());
                boolean enabled = Fadah.getINSTANCE().getConfigFile().getBoolean("enabled");
                Fadah.getINSTANCE().getConfigFile().getConfiguration().set("enabled", !enabled);
                Fadah.getINSTANCE().getConfigFile().save();

                String toggle = enabled ? Lang.ADMIN_TOGGLE_DISABLED.toFormattedString() : Lang.ADMIN_TOGGLE_ENABLED.toFormattedString();
                Bukkit.getConsoleSender().sendMessage(Lang.PREFIX.toFormattedString() + Lang.ADMIN_TOGGLE_REMOTE.toFormattedString(toggle));
            }

            default -> throw new IllegalStateException("Unexpected value: " + message.getType());
        }
    }

    public abstract void connect();

    protected abstract void send(@NotNull Message message);

    public abstract void destroy();

    @Getter
    @AllArgsConstructor
    public enum Type {
        REDIS("Redis"),
        ;
        private final String displayName;
    }
}
