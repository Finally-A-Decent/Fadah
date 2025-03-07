package info.preva1l.fadah.records.collection;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record CollectableItem(
        ItemStack itemStack,
        long dateAdded
) {
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
}
