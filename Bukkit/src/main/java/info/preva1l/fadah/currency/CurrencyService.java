package info.preva1l.fadah.currency;

import info.preva1l.fadah.utils.Text;
import info.preva1l.trashcan.flavor.annotations.Configure;
import info.preva1l.trashcan.flavor.annotations.Service;
import info.preva1l.trashcan.flavor.annotations.inject.Inject;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

@Service
public final class CurrencyService {
    public static final CurrencyService instance = new CurrencyService();

    @Inject public Logger logger;

    @Configure
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
