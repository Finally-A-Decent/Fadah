package info.preva1l.fadah.hooks.impl;

import info.preva1l.fadah.data.PermissionsData;
import info.preva1l.fadah.hooks.Hook;
import lombok.Getter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

@Getter
public class PapiHook extends Hook {
    @Override
    protected boolean onEnable() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        if (plugin == null || !plugin.isEnabled()) return false;
        return new Expansion().register();
    }

    @Getter
    private static class Expansion extends PlaceholderExpansion {
        public final String identifier = "fadah";
        public final String author = "Preva1l";
        public final String version = "1.0.0";
        @Override public boolean persist() { return true; }

        @Override
        public String onRequest(OfflinePlayer p, @NotNull String params) {
            if (!(p instanceof Player player)) return null;
            return switch (params) {
                case "listings_current" -> String.valueOf(PermissionsData.getCurrentListings(player));
                case "listings_max" -> String.valueOf(PermissionsData.getHighestDouble(PermissionsData.PermissionType.MAX_LISTINGS, player));
                default -> null;
            };
        }
    }
}
