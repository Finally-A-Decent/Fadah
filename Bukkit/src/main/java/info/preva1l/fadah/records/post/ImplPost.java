package info.preva1l.fadah.records.post;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.api.ListingCreateEvent;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.misc.Tuple;
import info.preva1l.fadah.data.DataService;
import info.preva1l.fadah.filters.MatcherService;
import info.preva1l.fadah.hooks.HookService;
import info.preva1l.fadah.hooks.impl.DiscordHook;
import info.preva1l.fadah.hooks.impl.permissions.Permission;
import info.preva1l.fadah.hooks.impl.permissions.PermissionsHook;
import info.preva1l.fadah.multiserver.Broker;
import info.preva1l.fadah.multiserver.Message;
import info.preva1l.fadah.multiserver.Payload;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.records.listing.ListingBuilder;
import info.preva1l.fadah.utils.Text;
import info.preva1l.fadah.utils.Tasks;
import info.preva1l.fadah.utils.TimeUtil;
import info.preva1l.fadah.utils.logging.TransactionLogger;
import info.preva1l.fadah.watcher.AuctionWatcher;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;

/**
 * Created on 7/03/2025
 *
 * @author Preva1l
 */
public final class ImplPost extends Post {
    private final @Nullable Player player;
    private final DecimalFormat df = Config.i().getFormatting().numbers();

    public ImplPost(ListingBuilder listing, @Nullable Player player) {
        super(listing);
        this.player = player;
    }

    @Override
    public PostResult buildAndSubmit() {
        if (bypassTax) listingBuilder.tax(0.0);

        Listing listing = listingBuilder.build();
        if (listing.getCategoryID().equals("_none_")) return PostResult.RESTRICTED_ITEM;
        if (isRestrictedItem(listing.getItemStack())) return PostResult.RESTRICTED_ITEM;

        if (!bypassMaxListings && player != null) {
            int currentListings = CacheAccess.amountByPlayer(Listing.class, player.getUniqueId());
            int maxListings = PermissionsHook.getValue(Integer.class, Permission.MAX_LISTINGS, player);

            if (currentListings >= maxListings) return PostResult.MAX_LISTINGS;
        }

        if (callEvent) {
            ListingCreateEvent event = new ListingCreateEvent(player, listing);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) return PostResult.custom(event.getCancelReason());
        }

        CacheAccess.add(Listing.class, listing);
        DataService.getInstance().save(Listing.class, listing);

        if (!processMessages(listing)) {
            return PostResult.SUCCESS_ADVERT_FAIL;
        }

        return PostResult.SUCCESS;
    }

    private boolean processMessages(Listing listing) {
        if (notifyPlayer) notifyPlayer(listing);
        if (submitLog) TransactionLogger.listingCreated(listing);

        HookService.getHook(DiscordHook.class).ifPresent(hook -> {
            if (!(hook.getConf().isOnlySendOnAdvert() && postAdvert)) hook.send(listing);
        });

        if (postAdvert && !postAdvert(listing, bypassAdvertCost)) {
            return false;
        }

        if (alertWatchers) AuctionWatcher.alertWatchers(listing);

        return true;
    }

    private boolean isRestrictedItem(ItemStack item) {
        for (String blacklist : Config.i().getBlacklists()) {
            boolean result = MatcherService.instance.process(blacklist, true, item); // restrict on failure just in case
            if (result) return true;
        }
        return false;
    }

    private void notifyPlayer(Listing listing) {
        if (player == null) return;

        double taxAmount = listing.getTax();
        Component message = Text.text(
                Lang.i().getNotifications().getNewListing(),
                Tuple.of("%item%", Text.extractItemName(listing.getItemStack())),
                Tuple.of("%price%", df.format(listing.getPrice())),
                Tuple.of("%time%", TimeUtil.formatTimeUntil(listing.getDeletionDate())),
                Tuple.of("%current_listings%", CacheAccess.amountByPlayer(Listing.class, player.getUniqueId())),
                Tuple.of("%max_listings%", PermissionsHook.getValue(Integer.class, Permission.MAX_LISTINGS, player)),
                Tuple.of("%tax%", taxAmount + ""),
                Tuple.of("%price_after_tax%", df.format((taxAmount / 100) * listing.getPrice()))
        );
        player.sendMessage(message);
    }

    private boolean postAdvert(Listing listing, boolean bypassAdvertCost) {
        if (player == null) return false;

        double advertPrice = PermissionsHook.getValue(Double.class, Permission.ADVERT_PRICE, player);
        if (!bypassAdvertCost && !listing.getCurrency().canAfford(player, advertPrice)) {
            Lang.sendMessage(player, Lang.i().getPrefix() + Lang.i().getErrors().getAdvertExpense());
            return false;
        }

        listing.getCurrency().withdraw(player, advertPrice);

        Component advertMessage = Text.text(
                Lang.i().getNotifications().getAdvert(),
                Tuple.of("%player%", player.getName()),
                Tuple.of("%item%", Text.extractItemName(listing.getItemStack())),
                Tuple.of("%price%", df.format(listing.getPrice())),
                Tuple.of("%listing_id%", listing.getId())
        );

        Tasks.sync(Fadah.getInstance(), () -> Bukkit.broadcast(advertMessage));

        if (Broker.getInstance().isConnected()) {
            Message.builder()
                    .type(Message.Type.NOTIFICATION)
                    .payload(Payload.withNotification(null, advertMessage))
                    .build().send(Broker.getInstance());
        }
        return true;
    }
}
