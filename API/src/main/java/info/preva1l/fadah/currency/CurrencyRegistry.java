package info.preva1l.fadah.currency;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class CurrencyRegistry {
    private static Map<Integer, String> enumerator = new ConcurrentHashMap<>();
    private static Map<String, Currency> values = new ConcurrentHashMap<>();

    public static void registerMulti(MultiCurrency currency) {
        if (!currency.getRequiredPlugin().isEmpty()) {
            Plugin requiredPlugin = Bukkit.getPluginManager().getPlugin(currency.getRequiredPlugin());
            if (requiredPlugin == null || !requiredPlugin.isEnabled()) return;
        }

        if (!currency.preloadChecks()) {
            Logger.getLogger("Fadah")
                    .severe("Tried enabling currency %s but the preload checks failed!"
                            .formatted(currency.getId().toLowerCase()));
            return;
        }

        for (Currency curr : currency.getCurrencies()) {
            register(curr);
        }
    }

    public static void register(Currency currency) {
        if (values == null) {
            values = new ConcurrentHashMap<>();
        }
        if (enumerator == null) {
            enumerator = new ConcurrentHashMap<>();
        }
        Logger.getLogger("Fadah").info("Currency Loaded: " + currency.getId());

        if (!currency.getRequiredPlugin().isEmpty()) {
            Plugin requiredPlugin = Bukkit.getPluginManager().getPlugin(currency.getRequiredPlugin());
            if (requiredPlugin == null || !requiredPlugin.isEnabled()) {
                Logger.getLogger("Fadah")
                        .warning("Tried enabling currency %s but the required plugin %s is not found/enabled!"
                                .formatted(currency.getId().toLowerCase(), currency.getRequiredPlugin()));
                return;
            }
        }

        if (!currency.preloadChecks()) {
            Logger.getLogger("Fadah")
                    .severe("Tried enabling currency %s but the preload checks failed!"
                            .formatted(currency.getId().toLowerCase()));
            return;
        }

        values.put(currency.getId().toLowerCase(), currency);
        enumerator.put(enumerator.size(), currency.getId().toLowerCase());
    }

    public static Currency get(String currencyCode) {
        if (values == null) {
            values = new ConcurrentHashMap<>();
        }
        return values.get(currencyCode.toLowerCase());
    }

    @Blocking
    public static void unregister(@NotNull Currency currency) {
        values.remove(currency.getId().toLowerCase());

        int removedKey = -1;
        for (Map.Entry<Integer, String> entry : enumerator.entrySet()) {
            if (entry.getValue().equals(currency.getId().toLowerCase())) {
                removedKey = entry.getKey();
                break;
            }
        }

        if (removedKey != -1) {
            enumerator.remove(removedKey);
            Map<Integer, String> updatedEnumerator = new LinkedHashMap<>();

            for (Map.Entry<Integer, String> entry : enumerator.entrySet()) {
                int currentKey = entry.getKey();
                int newKey = currentKey > removedKey ? currentKey - 1 : currentKey;
                updatedEnumerator.put(newKey, entry.getValue());
            }

            enumerator.clear();
            enumerator.putAll(updatedEnumerator);
        }
    }

    public static Currency getNext(@NotNull Currency current) {
        Integer currentIndex = null;
        for (Map.Entry<Integer, String> entry : enumerator.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(current.getId())) {
                currentIndex = entry.getKey();
                break;
            }
        }
        if (currentIndex == null || currentIndex == enumerator.size() - 1) {
            return null;
        }
        int nextIndex = currentIndex + 1;
        String nextCurrencyId = enumerator.get(nextIndex);
        return values.get(nextCurrencyId);
    }

    public static Currency getPrevious(@NotNull Currency current) {
        Integer currentIndex = null;
        for (Map.Entry<Integer, String> entry : enumerator.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(current.getId())) {
                currentIndex = entry.getKey();
                break;
            }
        }
        if (currentIndex == null || currentIndex == 0) {
            return null;
        }
        int previousIndex = currentIndex - 1;
        String previousCurrencyId = enumerator.get(previousIndex);
        return values.get(previousCurrencyId);
    }

    public static List<Currency> getAll() {
        return List.copyOf(values.values());
    }
}