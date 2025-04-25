package info.preva1l.fadah.records.collection;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An item that can be collected through the collection box or expired listings menus.
 * <br><br>
 * Created on 13/04/2024
 *
 * @author Preva1l
 * @param itemStack the item the player will receive.
 * @param dateAdded the epoch timestamp of when the item was added.
 */
public record CollectableItem(
        ItemStack itemStack,
        long dateAdded
) implements Comparable<CollectableItem> {
    /**
     * Create a collectable item that is timestamped at the current time.
     *
     * @param itemStack the item the player will receive.
     * @return the collectable item.
     */
    public static CollectableItem of(@NotNull ItemStack itemStack) {
        return new CollectableItem(itemStack, System.currentTimeMillis());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CollectableItem(ItemStack stack, long added)) {
            return added == this.dateAdded() && stack.equals(this.itemStack);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemStack, dateAdded);
    }

    @Override
    public int compareTo(@NotNull CollectableItem o) {
        return Long.compare(o.dateAdded, this.dateAdded);
    }
}
