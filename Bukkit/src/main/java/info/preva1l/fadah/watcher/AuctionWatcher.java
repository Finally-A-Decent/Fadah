package info.preva1l.fadah.watcher;

import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.ListHelper;
import info.preva1l.fadah.config.Tuple;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.utils.StringUtils;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class AuctionWatcher {
    @Getter private final Map<UUID, Watching> watchingListings = new ConcurrentHashMap<>();

    public void watch(@NotNull Watching watching) {
        watchingListings.put(watching.getPlayer(), watching);
    }

    public Optional<Watching> get(@NotNull UUID uuid) {
        return Optional.ofNullable(watchingListings.get(uuid));
    }

    @Blocking
    public void alertWatchers(@NotNull Listing listing) {
        for (Map.Entry<UUID, Watching> entry : watchingListings.entrySet()) {
            Watching watching = entry.getValue();
            if (watching.getSearch() != null) {
                if (StringUtils.doesItemHaveString(watching.getSearch().toUpperCase(), listing.getItemStack())
                        || checkForEnchantmentOnBook(watching.getSearch().toUpperCase(), listing.getItemStack())) {
                    if (priceCheck(listing, watching)) {
                        sendAlert(entry.getKey(), listing);
                        return;
                    }
                    sendAlert(entry.getKey(), listing);
                    return;
                }
            }

            if (priceCheck(listing, watching)) {
                sendAlert(entry.getKey(), listing);
            }
        }
    }

    private boolean priceCheck(Listing listing, Watching watching) {
        return (watching.getMinPrice() != -1 || watching.getMaxPrice() != -1) &&
                (watching.getMinPrice() == -1 || watching.getMinPrice() <= listing.getPrice()) &&
                (watching.getMaxPrice() == -1 || watching.getMaxPrice() >= listing.getPrice());
    }

    private void sendAlert(UUID uuid, Listing listing) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        String alertMessage = String.join("&r\n", ListHelper.replace(Lang.i().getNotifications().getWatched(),
                Tuple.of("%player%", player.getName()),
                Tuple.of("%item%", StringUtils.extractItemName(listing.getItemStack())),
                Tuple.of("%price%", new DecimalFormat(Config.i().getFormatting().getNumbers()).format(listing.getPrice()))
        ));

        player.sendMessage(StringUtils.colorize(alertMessage));
    }

    private boolean checkForEnchantmentOnBook(String enchant, ItemStack enchantedBook) {
        if (!Config.i().getSearch().isEnchantedBooks()) return false;
        if (enchantedBook.getType() == Material.ENCHANTED_BOOK) {
            for (Enchantment enchantment : enchantedBook.getEnchantments().keySet()) {
                if (enchantment.getKey().getKey().toUpperCase().contains(enchant)) return true;
            }
        }
        return false;
    }
}
