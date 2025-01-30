package info.preva1l.fadah.data;

import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.records.listing.Listing;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@UtilityClass
public class PermissionsData {
    public int getCurrentListings(Player player) {
        Map<UUID, Listing> listings = ListingCache.getListings();
        for (UUID key : new ArrayList<>(listings.keySet())) {
            Listing listing = listings.get(key);
            if (!listing.isOwner(player)) listings.remove(key);
        }
        return listings.size();
    }

    public int getHighestInt(PermissionType type, Player player) {
        int currentMax = 0;
        boolean matched = false;
        final Set<PermissionAttachmentInfo> finalEffectivePermissions = player.getEffectivePermissions(); // "Thread Safe"
        for (PermissionAttachmentInfo effectivePermission : finalEffectivePermissions) {
            if (!effectivePermission.getPermission().startsWith(type.permissionString)) continue;
            String numberStr = effectivePermission.getPermission().substring(type.permissionString.length());
            try {
                if (currentMax < Integer.parseInt(numberStr)) {
                    currentMax = Integer.parseInt(numberStr);
                    matched = true;
                }
            } catch (NumberFormatException ignored) {}
        }
        return matched ? currentMax : (int) type.def;
    }

    public double getHighestDouble(PermissionType type, Player player) {
        double currentMax = 0;
        boolean matched = false;
        final Set<PermissionAttachmentInfo> finalEffectivePermissions = player.getEffectivePermissions(); // "Thread Safe"
        for (PermissionAttachmentInfo effectivePermission : finalEffectivePermissions) {
            if (!effectivePermission.getPermission().startsWith(type.permissionString))
                continue;
            if(!effectivePermission.getValue())
                continue;
            String numberStr = effectivePermission.getPermission().substring(type.permissionString.length());
            try {
                if (currentMax < Double.parseDouble(numberStr)) {
                    currentMax = Double.parseDouble(numberStr);
                    matched = true;
                }
            } catch (NumberFormatException ignored) {}
        }
        return matched ? currentMax : (double) type.def;
    }

    @AllArgsConstructor
    public enum PermissionType {
        MAX_LISTINGS("fadah.max-listings.", Config.i().getDefaultMaxListings()),
        LISTING_TAX("fadah.listing-tax.", 0.00D),
        ADVERT_PRICE("fadah.advert-price.", Config.i().getListingAdverts().getDefaultPrice()),
        ;
        private final String permissionString;
        private final Number def;
    }
}
