package info.preva1l.fadah.data.handler;

import info.preva1l.fadah.data.fixers.v2.V2Fixer;
import info.preva1l.fadah.data.fixers.v3.V3Fixer;

import java.util.UUID;

public interface DatabaseHandler extends DataHandler {
    boolean isConnected();
    void connect();
    void destroy();
    void registerDaos();
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