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

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return id.equals(category.id);
    }
}