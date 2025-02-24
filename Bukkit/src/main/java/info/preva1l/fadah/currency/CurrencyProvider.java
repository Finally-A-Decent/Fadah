package info.preva1l.fadah.currency;

import info.preva1l.fadah.Fadah;

import java.util.stream.Stream;

public interface CurrencyProvider {
    default void loadCurrencies() {
        Fadah.getConsole().info("Loading currencies...");
        Stream.of(
                new VaultCurrency()
        ).forEach(CurrencyRegistry::register);
        Stream.of(
                new RedisEconomyCurrency(),
                new CoinsEngineCurrency()
        ).forEach(CurrencyRegistry::registerMulti);
        Fadah.getConsole().info("Currencies Loaded!");
    }
}
