package info.preva1l.fadah.metrics;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.records.listing.Listing;

import java.util.logging.Logger;

public final class MetricsService {
    public static final MetricsService instance = new MetricsService();

    private static final int METRICS_ID = 21651;

    private Fadah plugin;
    private Logger logger;

    private Metrics metrics;

    public MetricsService init(Fadah plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        return this;
    }

    public void configure() {
        logger.info("Starting Metrics...");

        metrics = new Metrics(plugin, METRICS_ID);
        metrics.addCustomChart(new Metrics.SingleLineChart("items_listed", () -> CacheAccess.size(Listing.class)));
        metrics.addCustomChart(new Metrics.SimplePie("database_type", () -> Config.i().getDatabase().getType().getFriendlyName()));
        metrics.addCustomChart(new Metrics.SimplePie(
                "multi_server",
                () -> Config.i().getBroker().isEnabled() ? Config.i().getBroker().getType().getDisplayName() : "None"
        ));


        logger.info("Metrics Logging Started!");
    }

    public void close() {
        if (metrics != null) {
            metrics.shutdown();
        }
    }
}
