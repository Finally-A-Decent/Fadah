package info.preva1l.fadah.api.managers;

import info.preva1l.fadah.cache.CategoryRegistry;
import info.preva1l.fadah.records.Category;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Created on 7/03/2025
 *
 * @author Preva1l
 */
public final class ImplCategoryManager implements CategoryManager {
    @Override
    public @Nullable Category get(@NotNull String id) {
        return CategoryRegistry.getCategory(id);
    }

    @Override
    public CompletableFuture<String> forItem(@NotNull ItemStack item) {
        return CategoryRegistry.getCategoryForItem(item);
    }

    @Override
    public boolean register(@NotNull Category category) {
        return CategoryRegistry.registerCategory(category);
    }

    @Override
    public boolean unregister(@NotNull String id) {
        return CategoryRegistry.unregisterCategory(id);
    }
}
