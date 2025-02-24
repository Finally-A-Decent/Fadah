package info.preva1l.fadah.data.fixers.v3;

import java.util.UUID;

public interface V3Fixer {
    static Empty empty() {
        return new Empty();
    }

    void fixHistory(UUID player);

    boolean needsFixing(UUID player);

    class Empty implements V3Fixer {
        @Override
        public void fixHistory(UUID player) {
            // do nothing
        }

        @Override
        public boolean needsFixing(UUID player) {
            return false;
        }
    }
}
