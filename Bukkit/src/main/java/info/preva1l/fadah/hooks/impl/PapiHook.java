package info.preva1l.fadah.hooks.impl;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.hooks.impl.permissions.Permission;
import info.preva1l.fadah.hooks.impl.permissions.PermissionsHook;
import info.preva1l.fadah.records.collection.CollectionBox;
import info.preva1l.fadah.records.collection.ExpiredItems;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.hooker.annotation.Hook;
import info.preva1l.hooker.annotation.OnStart;
import info.preva1l.hooker.annotation.Require;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@Hook(id = "placeholders")
@Require("PlaceholderAPI")
public class PapiHook {
    @OnStart
    public boolean onEnable() {
        return new Expansion().register();
    }

    public String format(@Nullable final Player player, final String string) {
        return PlaceholderAPI.setPlaceholders(player, string);
    }

    @Getter
    private static class Expansion extends PlaceholderExpansion {
        public final String identifier = "fadah";
        public final String author = "Preva1l";
        public final String version = Fadah.getInstance().getCurrentVersion().toString();
        @Override public boolean persist() { return true; }

        @Override
        public String onRequest(OfflinePlayer p, @NotNull String params) {
            return Placeholder.parse(p, params);
        }
    }

    static class Placeholder<T> {
        private static final Map<String, Placeholder<?>> placeholders = new HashMap<>();

        static {
            online("listings_max", player -> PermissionsHook.getValue(String.class, Permission.MAX_LISTINGS, player));
            online("expired", player -> CacheAccess.amountByPlayer(ExpiredItems.class, player.getUniqueId()));
            online("collectable", player -> CacheAccess.amountByPlayer(CollectionBox.class, player.getUniqueId()));

            offline("listings_current", player -> CacheAccess.amountByPlayer(Listing.class, player.getUniqueId()));

            any("listings_all", () -> CacheAccess.size(Listing.class));
            any("enabled", () -> Config.i().isEnabled());
        }

        private final Class<T> type;
        private final Function<T, Object> parser;

        private Placeholder(Class<T> type, Function<T, Object> parser) {
            this.type = type;
            this.parser = parser;
        }

        public static String parse(@Nullable OfflinePlayer player, @NotNull String params) {
            return String.valueOf(get(params).parse(player));
        }

        private static void online(String match, Function<Player, Object> parser) {
            placeholders.put(match, new Placeholder<>(Player.class, parser));
        }

        private static void offline(String match, Function<OfflinePlayer, Object> parser) {
            placeholders.put(match, new Placeholder<>(OfflinePlayer.class, parser));
        }

        private static void any(String match, Supplier<Object> parser) {
            placeholders.put(match, new Placeholder<>(Void.class, v -> parser.get()));
        }

        private static <E> Placeholder<E> get(String match) {
            if (!placeholders.containsKey(match)) return null;
            return (Placeholder<E>) placeholders.get(match);
        }

        private Object parse(T target) {
            if (!type.isInstance(target)) return null;
            return parser.apply(target);
        }
    }
}
