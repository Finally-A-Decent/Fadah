package info.preva1l.fadah.currency;

import dev.unnm3d.rediseconomy.api.RedisEconomyAPI;
import info.preva1l.fadah.config.CurrencySettings;
import info.preva1l.fadah.config.misc.SubEconomy;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class RedisEconomyCurrency implements MultiCurrency {
    private final String id = "redis_economy";
    private final String requiredPlugin = "RedisEconomy";
    private final List<Currency> currencies = new ArrayList<>();

    private RedisEconomyAPI api;

    @Override
    public boolean isEnabled() {
        return CurrencySettings.i().getVault().isEnabled();
    }

    @Override
    public boolean preloadChecks() {
        api = RedisEconomyAPI.getAPI();
        if (api == null) {
            CurrencyService.instance.logger.severe("-------------------------------------");
            CurrencyService.instance.logger.severe("Cannot enable redis economy currency!");
            CurrencyService.instance.logger.severe("Plugin did not start correctly.");
            CurrencyService.instance.logger.severe("-------------------------------------");
            return false;
        }
        for (SubEconomy eco : CurrencySettings.i().getRedisEconomy().getCurrencies()) {
            Currency subCur = new SubCurrency(
                    id + "_" + eco.economy(),
                    eco.displayName(),
                    eco.symbol(),
                    requiredPlugin,
                    (p, a) -> getCurrency(eco).withdrawPlayer(p, a).transactionSuccess(),
                    (p, a) -> getCurrency(eco).depositPlayer(p, a).transactionSuccess(),
                    p -> getCurrency(eco).getBalance(p),
                    v -> {
                        if (getCurrency(eco) == null) {
                            CurrencyService.instance.logger.severe("-------------------------------------");
                            CurrencyService.instance.logger.severe("Cannot enable redis economy currency!");
                            CurrencyService.instance.logger.severe("No currency with name: " + eco.economy());
                            CurrencyService.instance.logger.severe("-------------------------------------");
                            return false;
                        }
                        return true;
                    }
            );
            currencies.add(subCur);
        }
        return true;
    }

    private dev.unnm3d.rediseconomy.currency.Currency getCurrency(SubEconomy eco) {
        return api.getCurrencyByName(eco.economy());
    }
}
