package info.preva1l.fadah.api.managers;

import info.preva1l.fadah.records.Category;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Access to the category registry via the API.
 * </br>
 * Created on 7/03/2025
 *
 * @author Preva1l
 */
@SuppressWarnings("unused")
public interface CategoryManager {
    /**
     * Get a category by id.
     *
     * @return the category matching that ID, or null if no category with that id exists.
     * @since 2.9
     */
    @Nullable
    Category get(@NotNull String id);

    /**
     * Get a category for an item.
     * </br>
     * This is generally a slow operation as it has to process a-lot of information so it is a completable future.
     * It runs the item through all the categories (in order of priority) and runs all the matchers
     * to find the first category that accepts the item, if there is not one that matches it returns
     * the placeholder value for no category "{@code _none_}".
     *
     * @return the category id or "{@code _none_}"
     * @since 2.9
     */
    CompletableFuture<String> forItem(@NotNull ItemStack item);

    /**
     * Register a custom category via the api.
     * This is usually done via config, but I thought maybe some plugins may like to add a category for their items automatically.
     *
     * @return true if successful, false if a category with that id already exists
     * @since 2.9
     */
    boolean register(@NotNull Category category);

    /**
     * Unregister a category by its id.
     *
     * @return true if successful, false if the category never existed
     * @since 2.9
     */
    boolean unregister(@NotNull String id);
}
