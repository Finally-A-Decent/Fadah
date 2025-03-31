package info.preva1l.fadah.records.listing;

import info.preva1l.fadah.api.ListingPurchaseEvent;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.misc.Tuple;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.multiserver.Broker;
import info.preva1l.fadah.multiserver.Message;
import info.preva1l.fadah.multiserver.Payload;
import info.preva1l.fadah.records.collection.CollectableItem;
import info.preva1l.fadah.records.collection.CollectionBox;
import info.preva1l.fadah.utils.Text;
import info.preva1l.fadah.utils.logging.TransactionLogger;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

@Getter
public final class ImplBinListing extends ActiveListing implements BinListing {
    private final double price;

    public ImplBinListing(@NotNull UUID id, @NotNull UUID owner, @NotNull String ownerName,
                          @NotNull ItemStack itemStack, @NotNull String categoryID, @NotNull String currency, double price,
                          double tax, long creationDate, long deletionDate) {
        super(id, owner, ownerName, itemStack, categoryID, currency, tax, creationDate, deletionDate);
        this.price = price;
    }

    @Override
    public boolean canBuy(@NotNull Player player) {
        if (!getCurrency().canAfford(player, getPrice())) {
            Lang.sendMessage(player, Lang.i().getPrefix() + Lang.i().getErrors().getTooExpensive());
            return false;
        }

        return super.canBuy(player);
    }

    @Override
    public StaleListing getAsStale() {
        return new StaleListing(id, owner, ownerName, itemStack, categoryID, currencyId, price, tax, creationDate, deletionDate, new ConcurrentSkipListSet<>());
    }

    @Override
    public void purchase(@NotNull Player buyer) {
        if (!canBuy(buyer)) return;

        // Money Transfer
        getCurrency().withdraw(buyer, this.getPrice());
        double taxed = (this.getTax()/100) * this.getPrice();
        getCurrency().add(Bukkit.getOfflinePlayer(this.getOwner()), this.getPrice() - taxed);

        // Remove Listing
        CacheAccess.invalidate(Listing.class, this);
        DatabaseManager.getInstance().delete(Listing.class, this);

        // Add to collection box
        ItemStack itemStack = this.getItemStack().clone();
        CacheAccess.getNotNull(CollectionBox.class, buyer.getUniqueId()).add(new CollectableItem(itemStack, Instant.now().toEpochMilli()));

        // Notify Both Players
        Lang.sendMessage(buyer, String.join("\n", Lang.i().getNotifications().getNewItem()));

        String itemName = Text.extractItemName(itemStack);
        String formattedPrice = Config.i().getFormatting().numbers().format(this.getPrice() - taxed);
        Component message = Text.text(Lang.i().getNotifications().getSale(),
                Tuple.of("%item%", itemName),
                Tuple.of("%price%", formattedPrice),
                Tuple.of("%buyer%", buyer.getName()));

        Player seller = Bukkit.getPlayer(this.getOwner());
        if (seller != null) {
            seller.sendMessage(message);
        } else {
            if (Broker.getInstance().isConnected()) {
                Message.builder()
                        .type(Message.Type.NOTIFICATION)
                        .payload(Payload.withNotification(this.getOwner(), message))
                        .build().send(Broker.getInstance());
            }
        }

        TransactionLogger.listingSold(this, buyer);

        Bukkit.getServer().getPluginManager().callEvent(new ListingPurchaseEvent(this.getAsStale(), buyer));
    }
}
