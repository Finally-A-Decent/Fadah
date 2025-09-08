package info.preva1l.fadah.records.listing;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.api.ListingEndEvent;
import info.preva1l.fadah.api.ListingEndReason;
import info.preva1l.fadah.api.ListingPurchaseEvent;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.data.DataService;
import info.preva1l.fadah.multiserver.Broker;
import info.preva1l.fadah.multiserver.Message;
import info.preva1l.fadah.multiserver.Payload;
import info.preva1l.fadah.records.collection.*;
import info.preva1l.fadah.security.AwareDataService;
import info.preva1l.fadah.utils.Tasks;
import info.preva1l.fadah.utils.logging.TransactionLogger;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ActiveListing extends BaseListing {
    protected static final Logger LOGGER = Logger.getLogger(ActiveListing.class.getName());

    protected final AtomicBoolean ended = new AtomicBoolean(false);

    protected ActiveListing(@NotNull UUID id, @NotNull UUID owner, @NotNull String ownerName,
                            @NotNull ItemStack itemStack, @NotNull String currency,
                            double tax, long creationDate, long deletionDate) {
        super(id, owner, ownerName, itemStack, currency, tax, creationDate, deletionDate);
    }

    @Override
    public CompletableFuture<Void> expire(boolean force) {
        return AwareDataService.instance.execute(Listing.class, this, () -> expire0(force));
    }

    private void expire0(boolean force) {
        try {
            if (System.currentTimeMillis() <= deletionDate && !force) return;

            removeListing();
            addToExpiredItems();
            logExpiration();
            fireExpirationEvent();
            ended.set(true);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error expiring listing: " + getId(), e);
        }
    }

    private void removeListing() {
        try {
            CacheAccess.invalidate(Listing.class, this);
            DataService.getInstance().delete(Listing.class, this);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to remove listing from cache/database", e);
            throw e;
        }
    }

    private void addToExpiredItems() {
        try {
            CollectableItem collectableItem = new CollectableItem(getItemStack().clone(), System.currentTimeMillis());

            CacheAccess.get(ExpiredItems.class, getOwner())
                    .ifPresentOrElse(
                            cache -> {
                                try {
                                    cache.add(collectableItem);
                                } catch (Exception e) {
                                    LOGGER.log(Level.WARNING, "Failed to add to cached expired items", e);
                                }
                            },
                            () -> handleExpiredItemsFromDatabase(collectableItem)
                    );
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to add item to expired items", e);
        }
    }

    private void handleExpiredItemsFromDatabase(CollectableItem collectableItem) {
        try {
            DataService.getInstance().get(ExpiredItems.class, getOwner())
                    .thenCompose(items -> {
                        ExpiredItems expiredItems = items.orElseGet(() -> ImplExpiredItems.empty(getOwner()));
                        expiredItems.add(collectableItem);
                        return DataService.getInstance().save(ExpiredItems.class, expiredItems);
                    })
                    .exceptionally(throwable -> {
                        LOGGER.log(Level.SEVERE, "Failed to save expired items to database", throwable);
                        return null;
                    });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error handling expired items from database", e);
        }
    }

    protected void handleCollectionBoxFromDatabase(UUID owner, CollectableItem collectableItem) {
        try {
            DataService.getInstance().get(CollectionBox.class, owner)
                    .thenCompose(items -> {
                        CollectionBox collectionBox = items.orElseGet(() -> ImplCollectionBox.empty(owner));
                        collectionBox.add(collectableItem);
                        return DataService.getInstance().save(CollectionBox.class, collectionBox);
                    })
                    .exceptionally(throwable -> {
                        LOGGER.log(Level.SEVERE, "Failed to save expired items to database", throwable);
                        return null;
                    });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error handling expired items from database", e);
        }
    }

    private void logExpiration() {
        try {
            TransactionLogger.listingExpired(this);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to log listing expiration", e);
        }
    }

    private void fireExpirationEvent() {
        try {
            Tasks.sync(Fadah.getInstance(), () -> {
                try {
                    Bukkit.getServer().getPluginManager().callEvent(
                            new ListingEndEvent(this, ListingEndReason.EXPIRED)
                    );
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error firing expiration event", e);
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to schedule expiration event", e);
        }
    }

    @Override
    public CompletableFuture<Void> cancel(@NotNull Player canceller) {
        return AwareDataService.instance.execute(Listing.class, this, () -> cancel0(canceller));
    }

    protected void cancel0(@NotNull Player canceller) {
        try {
            sendCancellationMessage(canceller);
            removeListing();
            addItemToExpiredItems();

            boolean isAdmin = !this.isOwner(canceller);
            logCancellation(isAdmin);
            fireCancellationEvent(isAdmin);
            ended.set(true);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error cancelling listing: " + getId(), e);
        }
    }

    private void sendCancellationMessage(@NotNull Player canceller) {
        try {
            Lang.sendMessage(canceller, Lang.i().getPrefix() + Lang.i().getNotifications().getCancelled());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to send cancellation message", e);
        }
    }

    private void addItemToExpiredItems() {
        try {
            CollectableItem collectableItem = CollectableItem.of(itemStack.clone());

            CacheAccess.get(ExpiredItems.class, getOwner())
                    .ifPresentOrElse(
                            items -> {
                                try {
                                    items.add(collectableItem);
                                } catch (Exception e) {
                                    LOGGER.log(Level.WARNING, "Failed to add to cached expired items", e);
                                }
                            },
                            () -> handleCancelledItemToDatabase(collectableItem));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to add cancelled item to expired items", e);
        }
    }

    private void handleCancelledItemToDatabase(CollectableItem collectableItem) {
        try {
            DataService.getInstance()
                    .get(ExpiredItems.class, owner)
                    .thenAccept(expiredOpt -> {
                        try {
                            ExpiredItems expired = expiredOpt.orElseGet(() -> ImplExpiredItems.empty(owner));
                            expired.add(collectableItem);
                            DataService.getInstance().save(ExpiredItems.class, expired)
                                    .exceptionally(saveThrowable -> {
                                        LOGGER.log(Level.SEVERE, "Failed to save cancelled item", saveThrowable);
                                        return null;
                                    });
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "Error processing expired items", e);
                        }
                    })
                    .exceptionally(getThrowable -> {
                        LOGGER.log(Level.SEVERE, "Failed to get expired items", getThrowable);
                        return null;
                    });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in async cancelled item handling", e);
        }
    }

    private void logCancellation(boolean isAdmin) {
        try {
            TransactionLogger.listingRemoval(this, isAdmin);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to log listing cancellation", e);
        }
    }

    private void fireCancellationEvent(boolean isAdmin) {
        try {
            Tasks.sync(Fadah.getInstance(), () -> {
                try {
                    ListingEndReason reason = isAdmin ? ListingEndReason.CANCELLED_ADMIN : ListingEndReason.CANCELLED;
                    Bukkit.getServer().getPluginManager().callEvent(new ListingEndEvent(this, reason));
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error firing cancellation event", e);
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to schedule cancellation event", e);
        }
    }

    @Override
    public boolean canBuy(@NotNull Player player) {
        try {
            if (isOwner(player)) {
                Lang.sendMessage(player, Lang.i().getPrefix() + Lang.i().getErrors().getOwnListings());
                return false;
            }

            if (CacheAccess.get(Listing.class, getId()).isEmpty()) {
                Lang.sendMessage(player, Lang.i().getPrefix() + Lang.i().getErrors().getDoesNotExist());
                return false;
            }

            if (System.currentTimeMillis() >= getDeletionDate() || ended.get()) {
                Lang.sendMessage(player, Lang.i().getPrefix() + Lang.i().getErrors().getDoesNotExist());
                return false;
            }

            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking if player can buy listing", e);
            return false;
        }
    }

    @Override
    public boolean isActive() {
        try {
            if (CacheAccess.get(Listing.class, getId()).isEmpty()) return false;
            if (System.currentTimeMillis() >= getDeletionDate()) return false;
            if (ended.get()) return true;

            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking if listing is active", e);
            return false;
        }
    }

    protected void complete(Component message, OfflinePlayer buyer) {
        if (buyer == null) {
            LOGGER.log(Level.WARNING, "Buyer is null in complete method");
            return;
        }

        try {
            notifySeller(message);
            logSale(buyer);
            firePurchaseEvent(buyer);
            ended.set(true);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error completing listing sale", e);
        }
    }

    private void notifySeller(Component message) {
        try {
            Player seller = Bukkit.getPlayer(this.getOwner());
            if (seller != null && seller.isOnline()) {
                seller.sendMessage(message);
            } else {
                sendSellerNotificationViaNetwork(message);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to notify seller", e);
        }
    }

    private void sendSellerNotificationViaNetwork(Component message) {
        try {
            if (Broker.getInstance() != null && Broker.getInstance().isConnected()) {
                Message.builder()
                        .type(Message.Type.NOTIFICATION)
                        .payload(Payload.withNotification(this.getOwner(), message))
                        .build().send(Broker.getInstance());
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to send seller notification via network", e);
        }
    }

    private void logSale(OfflinePlayer buyer) {
        try {
            TransactionLogger.listingSold(this, buyer);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to log listing sale", e);
        }
    }

    private void firePurchaseEvent(OfflinePlayer buyer) {
        Tasks.sync(Fadah.instance, () -> {
            try {
                StaleListing staleListing = getAsStale();
                if (staleListing != null) {
                    Bukkit.getServer().getPluginManager().callEvent(new ListingPurchaseEvent(staleListing, buyer));
                } else {
                    LOGGER.log(Level.WARNING, "Failed to create stale listing for purchase event");
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to fire purchase event", e);
            }
        });
    }

    public abstract StaleListing getAsStale();
}