package info.preva1l.fadah.hooks.impl;

import info.preva1l.fadah.data.PermissionsData;
import info.preva1l.fadah.hooks.Hook;
import lombok.Getter;
import lombok.Setter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
public class PapiHook extends PlaceholderExpansion implements Hook {
    @Setter private boolean enabled = false;
    public final String identifier = "fadah";
    public final String author = "Preva1l";
    public final String version = "1.0.0";
    @Override public boolean persist() { return true; }

    public PapiHook() {
        this.register();
    }

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
