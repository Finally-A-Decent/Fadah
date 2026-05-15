package info.preva1l.fadah.security;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Created on 16/06/2025
 *
 * @author Preva1l
 */
public interface AwareDataProvider<T> {
    /**
     * This method only runs action if the object is safe to modify.
     */
    CompletableFuture<Void> execute(T obj, Supplier<CompletableFuture<Void>> action);
}
