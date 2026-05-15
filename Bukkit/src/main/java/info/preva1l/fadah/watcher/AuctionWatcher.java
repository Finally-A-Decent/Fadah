package info.preva1l.fadah.watcher;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.misc.Tuple;
import info.preva1l.fadah.multiserver.Broker;
import info.preva1l.fadah.multiserver.Message;
import info.preva1l.fadah.multiserver.Payload;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.utils.Text;
import info.preva1l.fadah.utils.Tasks;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

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

    public void alertWatchers(@NotNull Listing listing) {
        Tasks.sync(Fadah.getInstance(), () -> {
            for (Map.Entry<UUID, Watching> entry : watchingListings.entrySet()) {
                Watching watching = entry.getValue();
                if (watching.getSearch() != null) {
                    if (Text.doesItemHaveString(watching.getSearch().toUpperCase(), listing.getItemStack())
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
        });
    }

    private boolean priceCheck(Listing listing, Watching watching) {
        return (watching.getMinPrice() != -1 || watching.getMaxPrice() != -1) &&
                (watching.getMinPrice() == -1 || watching.getMinPrice() <= listing.getPrice()) &&
                (watching.getMaxPrice() == -1 || watching.getMaxPrice() >= listing.getPrice());
    }

    public void sendAlert(UUID uuid, Listing listing) {
        Component alertMessage = Text.text(Lang.i().getNotifications().getWatched(),
                Tuple.of("%player%", listing.getOwnerName()),
                Tuple.of("%item%", Text.extractItemName(listing.getItemStack())),
                Tuple.of("%price%", Config.i().getFormatting().numbers().format(listing.getPrice())),
                Tuple.of("%listing_id%", listing.getId())
        );

        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            if (Broker.getInstance().isConnected()) {
                Message.builder()
                        .type(Message.Type.NOTIFICATION)
                        .payload(Payload.withNotification(uuid, alertMessage))
                        .build().send(Broker.getInstance());
            }
            return;
        }

        Tasks.sync(Fadah.getInstance(), player, () -> player.sendMessage(alertMessage),
                () -> {
                    if (Broker.getInstance().isConnected()) {
                        Message.builder()
                                .type(Message.Type.NOTIFICATION)
                                .payload(Payload.withNotification(uuid, alertMessage))
                                .build().send(Broker.getInstance());
                    }
                });
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
