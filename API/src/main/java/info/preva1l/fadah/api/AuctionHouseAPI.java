package info.preva1l.fadah.api;

import info.preva1l.fadah.api.managers.CategoryManager;
import info.preva1l.fadah.api.managers.ListingManager;
import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.records.collection.CollectableItem;
import info.preva1l.fadah.records.collection.CollectionBox;
import info.preva1l.fadah.records.collection.ExpiredItems;
import info.preva1l.fadah.records.history.HistoricItem;
import info.preva1l.fadah.records.history.History;
import info.preva1l.fadah.records.listing.Listing;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public abstract class AuctionHouseAPI {
    private static AuctionHouseAPI instance = null;

    /**
     * Get the custom item filtering namespacedkey
     *
     * @return namespacedkey
     * @deprecated replaced with {@link info.preva1l.fadah.processor.ProcessorArgsRegistry} and category matchers
     */
    @Deprecated(since = "2.9", forRemoval = true)
    public NamespacedKey getCustomItemNameSpacedKey() {
        throw new UnsupportedOperationException();
    }

    /**
     * Set the custom item filtering namespacedkey
     *
     * @param key namespacedkey
     * @since 1.0
     * @deprecated replaced with {@link info.preva1l.fadah.processor.ProcessorArgsRegistry} and category matchers
     */
    @Deprecated(since = "2.9", forRemoval = true)
    public void setCustomItemNameSpacedKey(NamespacedKey key) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get a listing
     *
     * @param uuid a valid listing uuid
     * @return the listing or null if none found
     * @since 1.0
     * @deprecated replaced with {@link AuctionHouseAPI#listingManager()} & {@link ListingManager#get(UUID)}
     */
    @Deprecated(since = "2.9")
    public Listing getListing(UUID uuid) {
        return listingManager().get(uuid).orElse(null);
    }

    /**
     * Get the listing manager.
     * This manager allows you to modify listings and create new ones.
     *
     * @return the instance of the listing manager.
     * @since 2.9
     */
    public abstract ListingManager listingManager();

    /**
     * Get a category
     *
     * @param id a valid category id
     * @return the category or null if none found
     * @since 1.0
     * @deprecated replaced with {@link AuctionHouseAPI#categoryManager()} & {@link CategoryManager#get(String)}
     */
    @Deprecated(since = "2.9")
    public Category getCategory(String id) {
        return categoryManager().get(id);
    }

    /**
     * Get the category manager.
     * This manager allows you to get, register, unregister and find categories.
     *
     * @return the instance of the category manager.
     * @since 2.9
     */
    public abstract CategoryManager categoryManager();

    /**
     * Gets a reference to a player's collection box if they are online.
     * If you are trying to get the collection box of on offline user use {@link AuctionHouseAPI#loadCollectionBox(OfflinePlayer)}.
     *
     * @param player the player
     * @return the collection box of the user
     * @throws IllegalStateException when the user is not online.
     * @since 2.9
     */
    public CollectionBox collectionBox(Player player) throws IllegalStateException {
        return collectionBox(player.getUniqueId());
    }

    /**
     * Gets a reference to a player's collection box if they are online.
     * If you are trying to get the collection box of on offline user use {@link AuctionHouseAPI#loadCollectionBox(UUID)}.
     *
     * @param playerUniqueId the players uuid
     * @return the collection box of the user
     * @throws IllegalStateException when the user is not online.
     * @since 2.9
     */
    public abstract CollectionBox collectionBox(UUID playerUniqueId) throws IllegalStateException;

    /**
     * Loads a users collection box from the database,
     * if the user is online it uses {@link AuctionHouseAPI#collectionBox(UUID)}.
     *
     * @param player the offline player
     * @return the collection box of the user
     * @since 2.9
     */
    public CompletableFuture<CollectionBox> loadCollectionBox(OfflinePlayer player) {
        return loadCollectionBox(player.getUniqueId());
    }

    /**
     * Loads a users collection box from the database,
     * if the user is online it uses {@link AuctionHouseAPI#collectionBox(UUID)}.
     *
     * @param playerUniqueId the players uuid
     * @return the collection box of the user
     * @since 2.9
     */
    public abstract CompletableFuture<CollectionBox> loadCollectionBox(UUID playerUniqueId);

    /**
     * Get a players collection box
     *
     * @param offlinePlayer a player
     * @return the collection box or null if no items found for that player
     * @since 1.0
     * @deprecated replaced with {@link AuctionHouseAPI#collectionBox(Player)}
     */
    @Deprecated(since = "2.9")
    public List<CollectableItem> getCollectionBox(OfflinePlayer offlinePlayer) {
        return collectionBox(offlinePlayer.getUniqueId()).collectableItems();
    }

    /**
     * Get a players collection box
     *
     * @param uuid a players uuid
     * @return the collection box or null if no items found for that player
     * @since 1.0
     * @deprecated replaced with {@link AuctionHouseAPI#collectionBox(UUID)}
     */
    @Deprecated(since = "2.9")
    public List<CollectableItem> getCollectionBox(UUID uuid) {
        return collectionBox(uuid).collectableItems();
    }

    /**
     * Gets a reference to a player's expired items if they are online.
     * If you are trying to get the expired items of on offline user use {@link AuctionHouseAPI#loadExpiredItems(OfflinePlayer)}.
     *
     * @param player the player
     * @return the users expired items
     * @throws IllegalStateException when the user is not online.
     * @since 2.9
     */
    public ExpiredItems expiredItems(Player player) throws IllegalStateException {
        return expiredItems(player.getUniqueId());
    }

    /**
     * Gets a reference to a player's expired items if they are online.
     * If you are trying to get the expired items of on offline user use {@link AuctionHouseAPI#loadExpiredItems(UUID)}.
     *
     * @param playerUniqueId the players uuid
     * @return the users expired items
     * @throws IllegalStateException when the user is not online.
     * @since 2.9
     */
    public abstract ExpiredItems expiredItems(UUID playerUniqueId) throws IllegalStateException;

    /**
     * Loads a users expired items from the database,
     * if the user is online it uses {@link AuctionHouseAPI#expiredItems(Player)}.
     *
     * @param player the offline player
     * @return the users expired items
     * @since 2.9
     */
    public CompletableFuture<ExpiredItems> loadExpiredItems(OfflinePlayer player) {
        return loadExpiredItems(player.getUniqueId());
    }

    /**
     * Loads a users expired items from the database,
     * if the user is online it uses {@link AuctionHouseAPI#expiredItems(UUID)}.
     *
     * @param playerUniqueId the players uuid
     * @return the users expired items
     * @since 2.9
     */
    public abstract CompletableFuture<ExpiredItems> loadExpiredItems(UUID playerUniqueId);

    /**
     * Get a players expired items
     *
     * @param offlinePlayer a player
     * @return the expired items or null if no items found for that player
     * @since 1.0
     * @deprecated replaced with {@link AuctionHouseAPI#expiredItems(Player)}
     */
    @Deprecated(since = "2.9")
    public List<CollectableItem> getExpiredItems(OfflinePlayer offlinePlayer) {
        return expiredItems(offlinePlayer.getUniqueId()).expiredItems();
    }

    /**
     * Get a players expired items
     *
     * @param uuid a players uuid
     * @return the expired items or null if no items found for that player
     * @since 1.0
     * @deprecated replaced with {@link AuctionHouseAPI#expiredItems(UUID)}
     */
    @Deprecated(since = "2.9")
    public List<CollectableItem> getExpiredItems(UUID uuid) {
        return expiredItems(uuid).expiredItems();
    }

    /**
     * Gets a reference to a player's history if they are online.
     * If you are trying to get the history of on offline user use {@link AuctionHouseAPI#loadHistory(OfflinePlayer)}.
     *
     * @param player the player
     * @return the users history
     * @throws IllegalStateException when the user is not online.
     * @since 2.9
     */
    public History history(Player player) throws IllegalStateException {
        return history(player.getUniqueId());
    }

    /**
     * Gets a reference to a player's history if they are online.
     * If you are trying to get the history of on offline user use {@link AuctionHouseAPI#loadHistory(UUID)}.
     *
     * @param playerUniqueId the players uuid
     * @return the users history
     * @throws IllegalStateException when the user is not online.
     * @since 2.9
     */
    public abstract History history(UUID playerUniqueId) throws IllegalStateException;

    /**
     * Loads a users history from the database,
     * if the user is online it uses {@link AuctionHouseAPI#history(Player)}.
     *
     * @param player the offline player
     * @return the users history
     * @since 2.9
     */
    public CompletableFuture<History> loadHistory(OfflinePlayer player) {
        return loadHistory(player.getUniqueId());
    }

    /**
     * Loads a users history from the database,
     * if the user is online it uses {@link AuctionHouseAPI#history(UUID)}.
     *
     * @param playerUniqueId the players uuid
     * @return the users history
     * @since 2.9
     */
    public abstract CompletableFuture<History> loadHistory(UUID playerUniqueId);

    /**
     * Get a players history
     *
     * @param offlinePlayer a player
     * @return the players history, ordered from newest to oldest
     * @deprecated replaced with {@link AuctionHouseAPI#history(Player)}
     */
    @Deprecated(since = "2.9")
    public List<HistoricItem> getHistory(OfflinePlayer offlinePlayer) {
        return history(offlinePlayer.getUniqueId()).historicItems();
    }

    /**
     * Get a players history
     *
     * @param uuid a player uuid
     * @return the players history, ordered from newest to oldest
     * @deprecated replaced with {@link AuctionHouseAPI#history(UUID)}
     */
    @Deprecated(since = "2.9")
    public List<HistoricItem> getHistory(UUID uuid) {
        return history(uuid).historicItems();
    }

    /**
     * Get the locale for a logged action
     *
     * @param action action
     * @return the locale
     */
    @ApiStatus.Internal
    public abstract String getLoggedActionLocale(HistoricItem.LoggedAction action);

    /**
     * Get the instance of the Fadah API.
     *
     * @return the Fadah API implementation.
     * @throws IllegalStateException if you accessed the API incorrectly
     */
    public static AuctionHouseAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                    """
                    The Fadah API is not initialized yet!
                    This could be a couple things:
                    1. You are not depending/soft-depending on Fadah
                       - Check your plugin.yml
                    2. You are trying to access the API before onEnable
                       - Check for usages of AuctionHouseAPI#getInstance() in static constructors or in onLoad
                    3. You are shading/implementing the API instead of compiling against it
                       - If your using gradle make sure your using compileOnly instead of implementation
                       - If your using maven make sure your dependency is declared as provided
                    """.stripIndent());
        }
        return instance;
    }

    /**
     * Set the instance of the Fadah API.
     *
     * @throws IllegalStateException if the instance is already assigned
     */
    @ApiStatus.Internal
    public static void setInstance(AuctionHouseAPI newInstance) {
        if (instance != null) {
            throw new IllegalStateException("Instance has already been set");
        }
        instance = newInstance;
    }
}
