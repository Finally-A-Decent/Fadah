package info.preva1l.fadah.multiserver;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.data.DatabaseType;
import info.preva1l.fadah.utils.Tasks;
import info.preva1l.fadah.utils.guis.FastInvManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public abstract class Broker {
    private static Broker instance;
    @Getter private boolean connected = false;

    protected static final Object DUMMY_VALUE = new Object();

    protected final Fadah plugin;
    protected final Gson gson;
    protected final Cache<Integer, Object> cachedIds;

    protected Broker(@NotNull Fadah plugin) {
        this.plugin = plugin;
        this.gson = GsonComponentSerializer.gson().serializer();
        this.cachedIds = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).build();
    }

    protected void handle(@NotNull Message message) {
        if (message.getId() != null && cachedIds.getIfPresent(message.getId()) != null) return;
        switch (message.getType()) {
            case NOTIFICATION -> message.getPayload()
                    .getNotification().ifPresentOrElse(notification -> {
                        if (notification.getTarget() == null) {
                            Tasks.sync(plugin, () -> Bukkit.broadcast(notification.getMessage()));
                            return;
                        }
                        Player player = Bukkit.getPlayer(notification.getTarget());
                        if (player == null) return;

                        Tasks.sync(plugin, player, () -> player.sendMessage(notification.getMessage()), () -> {});
                    }, () -> {
                        throw new IllegalStateException("Notification message received with no notification info!");
                    });

            case RELOAD -> {
                Tasks.sync(plugin, () -> {
                    Fadah.getInstance().reload();
                    Lang.sendMessage(Bukkit.getConsoleSender(), Lang.i().getPrefix() + Lang.i().getCommands().getReload().getRemote());
                });
            }

            case TOGGLE -> {
                Tasks.sync(plugin, () -> {
                    FastInvManager.closeAll();
                    boolean enabled = Config.i().isEnabled();
                    Config.i().setEnabled(!enabled);

                    String toggle = enabled ? Lang.i().getCommands().getToggle().getDisabled() : Lang.i().getCommands().getToggle().getEnabled();
                    Lang.sendMessage(Bukkit.getConsoleSender(), Lang.i().getPrefix() + Lang.i().getCommands().getToggle().getRemote()
                            .replace("%status%", toggle));
                });
            }

            default -> throw new IllegalStateException("Unexpected value: " + message.getType());
        }
    }

    public abstract void connect();

    protected abstract void send(@NotNull Message message);

    public abstract void destroy();

    @Getter
    @AllArgsConstructor
    public enum Type {
        REDIS("Redis"),
        ;
        private final String displayName;
    }

    public void load() {
        Config.Broker settings = Config.i().getBroker();
        if (settings.isEnabled()) {
            plugin.getLogger().info("Connecting to Broker...");
            plugin.getLogger().info("Broker Type: %s".formatted(settings.getType().getDisplayName()));
            if (Config.i().getDatabase().getType() == DatabaseType.SQLITE) {
                plugin.getLogger().severe("------------------------------------------");
                plugin.getLogger().severe("Broker has not been enabled as the selected");
                plugin.getLogger().severe("       database is not compatible!");
                plugin.getLogger().severe("------------------------------------------");
                return;
            }
            connect();
            connected = true;
            plugin.getLogger().info("Successfully connected to broker!");
            return;
        }
        plugin.getLogger().info("Not connecting to broker. (Not Enabled)");
    }

    public static Broker getInstance() {
        if (instance == null) {
            instance = switch (Config.i().getBroker().getType()) {
                case REDIS -> new RedisBroker(Fadah.getInstance());
            };
        }
        return instance;
    }
}
