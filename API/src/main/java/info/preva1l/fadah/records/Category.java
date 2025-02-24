package info.preva1l.fadah.records;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record Category(
        @NotNull String id,
        @NotNull String name,
        int priority,
        int modelData,
        @NotNull Material icon,
        @NotNull List<String> description,
        @NotNull List<String> matchers
) implements Comparable<Category> {
    @Override
    public int compareTo(@NotNull Category o) {
        return Integer.compare(o.priority, this.priority);
    }
}