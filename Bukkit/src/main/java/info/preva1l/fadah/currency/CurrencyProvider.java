package info.preva1l.fadah.currency;

import info.preva1l.fadah.Fadah;
import info.preva1l.trashcan.plugin.annotations.PluginEnable;

import java.util.stream.Stream;

public interface CurrencyProvider {
    @PluginEnable
    static void loadCurrencies() {
        Fadah.getConsole().info("Loading currencies...");
        Stream.of(
                new VaultCurrency(),
                new PlayerPointsCurrency(),
                new RedisEconomyCurrency(),
                new CoinsEngineCurrency()
        ).forEach(CurrencyRegistry::register);
        Fadah.getConsole().info("Currencies Loaded!");
    }
}
