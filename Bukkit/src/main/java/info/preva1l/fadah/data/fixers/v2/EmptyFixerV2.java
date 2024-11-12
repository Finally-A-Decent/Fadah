package info.preva1l.fadah.data.fixers.v2;

import java.util.UUID;

public class EmptyFixerV2 implements V2Fixer {
    private static V2Fixer instance;

    @Override
    public void fixExpiredItems(UUID player) {
        // do nothing
    }

    @Override
    public void fixCollectionBox(UUID player) {
        // do nothing
    }

    @Override
    public boolean needsFixing(UUID player) {
        return false;
    }

    public static V2Fixer get() {
        return instance == null ? instance = new EmptyFixerV2() : instance;
    }
}
