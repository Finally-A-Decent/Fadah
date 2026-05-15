package info.preva1l.fadah.config.upgraders;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.upgraders.impl.CurrencyConfigUpgrader;
import info.preva1l.fadah.config.upgraders.impl.MatcherUpgrader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created on 16/06/2025
 *
 * @author Preva1l
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigUpgraderService {
    public static final ConfigUpgraderService instance = new ConfigUpgraderService();

    private Logger logger;

    public ConfigUpgraderService init(Fadah plugin) {
        this.logger = plugin.getLogger();
        return this;
    }

    public void configure() {
        Stream.of(
                new CurrencyConfigUpgrader(logger),
                new MatcherUpgrader(logger)
        ).forEach(ConfigUpgrader::migrate);
    }
}
