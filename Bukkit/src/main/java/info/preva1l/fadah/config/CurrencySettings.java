package info.preva1l.fadah.config;

import de.exlll.configlib.*;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.misc.SubEconomy;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Created on 16/06/2025
 *
 * @author Preva1l
 */
@Getter
@Configuration
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("FieldMayBeFinal")
public class CurrencySettings {
    private static CurrencySettings instance;

    private static final String CONFIG_HEADER = """
            #########################################
            #                Fadah                  #
            #     Multi-Currency Configuration      #
            #########################################
            """;

    private static final YamlConfigurationProperties PROPERTIES = YamlConfigurationProperties.newBuilder()
            .charset(StandardCharsets.UTF_8)
            .setNameFormatter(NameFormatters.LOWER_KEBAB_CASE)
            .header(CONFIG_HEADER).build();

    private String defaultCurrency = "vault";

    private Vault vault = new Vault();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Vault {
        private boolean enabled = true;
        private String name = "Money";
        private char symbol = '$';
    }

    private CoinsEngine coinsEngine = new CoinsEngine();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CoinsEngine {
        private boolean enabled = true;
        @Comment("Which currencies to use from coins engine.")
        private List<SubEconomy> currencies = List.of(
                new SubEconomy("mob_coins", "Mob Coins", '$'),
                new SubEconomy("coins", "Coins", '$'));
    }

    private RedisEconomy redisEconomy = new RedisEconomy();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class RedisEconomy {
        private boolean enabled = true;
        @Comment("Which currencies to use from redis economy, if your using the default currency use the vault currency instead.")
        private List<SubEconomy> currencies = List.of(
                new SubEconomy("dollar", "Dollar", '$'),
                new SubEconomy("euro", "Euro", '€'));
    }

    private PlayerPoints playerPoints = new PlayerPoints();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class PlayerPoints {
        private boolean enabled = true;
        private String name = "Points";
        private char symbol = '$';
    }

    public void save() {
        YamlConfigurations.save(new File(Fadah.getInstance().getDataFolder(), "currencies.yml").toPath(), CurrencySettings.class, this);
    }

    public static void reload() {
        instance = YamlConfigurations.load(new File(Fadah.getInstance().getDataFolder(), "currencies.yml").toPath(), CurrencySettings.class, PROPERTIES);
    }

    public static CurrencySettings i() {
        if (instance != null) {
            return instance;
        }

        return instance = YamlConfigurations.update(new File(Fadah.getInstance().getDataFolder(), "currencies.yml").toPath(), CurrencySettings.class, PROPERTIES);
    }
}
