package info.preva1l.fadah.currency;

import info.preva1l.fadah.utils.Text;
import info.preva1l.fadah.Fadah;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

public final class CurrencyService {
    public static final CurrencyService instance = new CurrencyService();

    public Logger logger;

    public CurrencyService init(Fadah plugin) {
        this.logger = plugin.getLogger();
        return this;
    }

    public void loadCurrencies() {
        Stream.of(
                new VaultCurrency(),
                new PlayerPointsCurrency(),
                new RedisEconomyCurrency(),
                new CoinsEngineCurrency()
        ).forEach(CurrencyRegistry::register);

        if (CurrencyRegistry.getAll().isEmpty()) {
            Text.list(List.of(
                    "&4&l---------------------------------------",
                    "&c      No Economy Plugin Installed!",
                    "&cPlugin will not work without custom hook!",
                    "&4&l---------------------------------------")
            ).forEach(Bukkit.getConsoleSender()::sendMessage);
        }
    }
}
