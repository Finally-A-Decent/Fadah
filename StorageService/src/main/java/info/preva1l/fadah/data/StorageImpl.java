package info.preva1l.fadah.data;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StorageImpl {
    private static StorageImpl instance;

    public static StorageImpl getInstance() {
        return instance = new StorageImpl();
    }


}
