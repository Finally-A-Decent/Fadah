package info.preva1l.fadah.currency;

import info.preva1l.fadah.api.AuctionHouseAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class CurrencyRegistry {
    private static Map<Integer, String> enumerator = new ConcurrentHashMap<>();
    private static Map<String, Currency> values = new ConcurrentHashMap<>();

    public static void registerMulti(MultiCurrency currency) {
        if (!currency.getRequiredPlugin().isEmpty()) {
            Plugin requiredPlugin = Bukkit.getPluginManager().getPlugin(currency.getRequiredPlugin());
            if (requiredPlugin == null || !requiredPlugin.isEnabled()) {
                AuctionHouseAPI.getInstance()
                        .verboseWarning("Tried enabling currency %s but the required plugin %s is not found/enabled!"
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

    public static void unregister(Currency currency) {
        values.remove(currency.getId().toLowerCase());
        enumerator.entrySet().removeIf(e -> e.getValue().equals(currency.getId().toLowerCase()));
    }

    public static Currency getNext(Currency current) {
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

    public static Currency getPrevious(Currency current) {
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
}