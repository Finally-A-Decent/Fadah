package info.preva1l.fadah.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface Cache<T> {
    void add(@Nullable T obj);

    @Nullable
    T get(UUID uuid);

    void invalidate(@NotNull UUID uuid);

    void invalidate(@NotNull T obj);

    @NotNull List<T> getAll();
}
