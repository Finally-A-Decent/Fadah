package info.preva1l.fadah.security;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.records.collection.CollectableItem;
import info.preva1l.fadah.records.collection.CollectionBox;
import info.preva1l.fadah.records.collection.ExpiredItems;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.security.impl.CollectionBoxAwareDataProvider;
import info.preva1l.fadah.security.impl.ExpiredListingsAwareDataProvider;
import info.preva1l.fadah.security.impl.ListingAwareDataProvider;
import info.preva1l.fadah.utils.Tasks;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * A service for modifying data that is aware of updates from other servers and prevents desync.
 * <p>
 * Created on 16/06/2025
 *
 * @author Preva1l
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AwareDataService {
    public static final AwareDataService instance = new AwareDataService();

    private final Map<Class<?>, AwareDataProvider<?>> providers = new HashMap<>();
    private final Map<Class<?>, AwareCollectableDataProvider<?>> collectableProviders = new HashMap<>();

    public void configure() {
        providers.clear();
        collectableProviders.clear();

        providers.put(Listing.class, new ListingAwareDataProvider());
        collectableProviders.put(CollectionBox.class, new CollectionBoxAwareDataProvider());
        collectableProviders.put(ExpiredItems.class, new ExpiredListingsAwareDataProvider());
    }

    public <T> void execute(Class<T> clazz, T obj, CollectableItem item, Runnable action) {
        getProviderCollectable(clazz).execute(obj, item, action);
    }

    public <T> CompletableFuture<Void> execute(Class<T> clazz, T obj, Runnable action) {
        return getProvider(clazz).execute(obj, () -> Tasks.syncFuture(Fadah.getInstance(), action));
    }

    public <T> CompletableFuture<Void> execute(Entity entity, Class<T> clazz, T obj, Runnable action) {
        return getProvider(clazz).execute(obj, () -> Tasks.syncFuture(Fadah.getInstance(), entity, action));
    }

    private <T> AwareDataProvider<T> getProvider(Class<T> clazz) {
        return (AwareDataProvider<T>) providers.get(clazz);
    }

    private <T> AwareCollectableDataProvider<T> getProviderCollectable(Class<T> clazz) {
        return (AwareCollectableDataProvider<T>) collectableProviders.get(clazz);
    }
}
