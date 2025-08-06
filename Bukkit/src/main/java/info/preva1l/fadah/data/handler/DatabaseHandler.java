package info.preva1l.fadah.data.handler;

import info.preva1l.fadah.data.dao.Dao;
import info.preva1l.fadah.data.fixers.v2.V2Fixer;
import info.preva1l.fadah.data.fixers.v3.V3Fixer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface DatabaseHandler {
    default <T> List<T> getAll(Class<T> clazz) {
        return (List<T>) getDao(clazz).getAll();
    }

    default <T> Optional<T> get(Class<T> clazz, UUID id) {
        return (Optional<T>) getDao(clazz).get(id);
    }

    default <T> void save(Class<T> clazz, T t) {
        getDao(clazz).save(t);
    }

    default <T> void update(Class<T> clazz, T t, Map<String, ?> params) {
        getDao(clazz).update(t, params);
    }

    default <T> void delete(Class<T> clazz, T t) {
        getDao(clazz).delete(t);
    }

    boolean isConnected();
    void connect();
    void destroy();
    void registerDaos();

    /**
     * Gets the DAO for a specific class.
     *
     * @param clazz The class to get the DAO for.
     * @param <T>   The type of the class.
     * @return The DAO for the specified class.
     */
    <T> Dao<T> getDao(Class<?> clazz);

    default void wipeDatabase() {
        throw new UnsupportedOperationException();
    }

    V2Fixer getV2Fixer();
    V3Fixer getV3Fixer();

    default void fixData(UUID player) {
        if (getV2Fixer().needsFixing(player)) {
            getV2Fixer().fixCollectionBox(player);
            getV2Fixer().fixExpiredItems(player);
        }
        if (getV3Fixer().needsFixing(player)) {
            getV3Fixer().fixHistory(player);
        }
    }
}