package info.preva1l.fadah.records.post;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.api.ListingCreateEvent;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.ListHelper;
import info.preva1l.fadah.config.Tuple;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.data.PermissionsData;
import info.preva1l.fadah.filters.Restrictions;
import info.preva1l.fadah.hooks.HookManager;
import info.preva1l.fadah.hooks.impl.DiscordHook;
import info.preva1l.fadah.multiserver.Broker;
import info.preva1l.fadah.multiserver.Message;
import info.preva1l.fadah.multiserver.Payload;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.records.listing.ListingBuilder;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.TaskManager;
import info.preva1l.fadah.utils.TimeUtil;
import info.preva1l.fadah.utils.logging.TransactionLogger;
import info.preva1l.fadah.watcher.AuctionWatcher;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Created on 7/03/2025
 *
 * @author Preva1l
 */
public final class ImplPost extends Post {
    private final @Nullable Player player;
    private final DecimalFormat df = new DecimalFormat(Config.i().getFormatting().getNumbers());

    public ImplPost(ListingBuilder listing, @Nullable Player player) {
        super(listing);
        this.player = player;
    }

    @Override
    public CompletableFuture<PostResult> buildAndSubmit() {
        ExecutorService executor = DatabaseManager.getInstance().getThreadPool();

        if (bypassTax) listingBuilder.tax(0.0);

        return listingBuilder.build().thenComposeAsync(listing ->
                Restrictions.isRestrictedItem(listing.getItemStack()).thenApplyAsync(restricted ->
                        (!restricted || bypassRestrictedItems) ? listing : null, executor), executor
        ).thenComposeAsync(listing -> {
            if (listing == null) return CompletableFuture.completedFuture(PostResult.RESTRICTED_ITEM);

            if (!bypassMaxListings && player != null &&
                    PermissionsData.getCurrentListings(player) >= PermissionsData.getHighestInt(PermissionsData.PermissionType.MAX_LISTINGS, player)) {
                return CompletableFuture.completedFuture(PostResult.MAX_LISTINGS);
            }

            if (callEvent) {
                ListingCreateEvent event = new ListingCreateEvent(player, listing);
                TaskManager.Sync.run(Fadah.getINSTANCE(), () -> Bukkit.getServer().getPluginManager().callEvent(event));
                if (event.isCancelled()) return CompletableFuture.completedFuture(PostResult.custom(event.getCancelReason()));
            }

            CacheAccess.add(Listing.class, listing);
            DatabaseManager.getInstance().save(Listing.class, listing);

            if (notifyPlayer) notifyPlayer(listing);
            if (submitLog) TransactionLogger.listingCreated(listing);

            HookManager.i().getHook(DiscordHook.class).ifPresent(hook -> {
                if (!(hook.getConf().isOnlySendOnAdvert() && postAdvert)) hook.send(listing);
            });

            if (postAdvert && !postAdvert(listing, bypassAdvertCost)) {
                return CompletableFuture.completedFuture(PostResult.SUCCESS_ADVERT_FAIL);
            }

            if (alertWatchers) AuctionWatcher.alertWatchers(listing);
            return CompletableFuture.completedFuture(PostResult.SUCCESS);
        }, executor);
    }

    private void notifyPlayer(Listing listing) {
        if (player == null) return;

        double taxAmount = PermissionsData.getHighestDouble(PermissionsData.PermissionType.LISTING_TAX, player);
        String itemName = StringUtils.extractItemName(listing.getItemStack());
        String message = String.join("\n", ListHelper.replace(
                Lang.i().getNotifications().getNewListing(),
                Tuple.of("%item%", itemName),
                Tuple.of("%price%", df.format(listing.getPrice())),
                Tuple.of("%time%", TimeUtil.formatTimeUntil(listing.getDeletionDate())),
                Tuple.of("%current_listings%", PermissionsData.getCurrentListings(player) + ""),
                Tuple.of("%max_listings%", PermissionsData.getHighestInt(PermissionsData.PermissionType.MAX_LISTINGS, player) + ""),
                Tuple.of("%tax%", taxAmount + ""),
                Tuple.of("%price_after_tax%", df.format((taxAmount / 100) * listing.getPrice()))
        ));
        Lang.sendMessage(player, message);
    }

    private boolean postAdvert(Listing listing, boolean bypassAdvertCost) {
        if (player == null) return false;

        double advertPrice = PermissionsData.getHighestDouble(PermissionsData.PermissionType.ADVERT_PRICE, player);
        if (!bypassAdvertCost && !listing.getCurrency().canAfford(player, advertPrice)) {
            Lang.sendMessage(player, Lang.i().getPrefix() + Lang.i().getErrors().getAdvertExpense());
            return false;
        }

        listing.getCurrency().withdraw(player, advertPrice);

        String advertMessage = String.join("&r\n", ListHelper.replace(
                Lang.i().getNotifications().getAdvert(),
                Tuple.of("%player%", player.getName()),
                Tuple.of("%item%", StringUtils.extractItemName(listing.getItemStack())),
                Tuple.of("%price%", df.format(listing.getPrice()))
        ));

        Component textComponent = MiniMessage.miniMessage().deserialize(
                StringUtils.legacyToMiniMessage(advertMessage));
        textComponent = textComponent.clickEvent(
                ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/ah view-listing " + listing.getId()));

        for (Player announce : Bukkit.getOnlinePlayers()) {
            Fadah.getINSTANCE().getAdventureAudience().player(announce).sendMessage(textComponent);
        }

        if (Broker.getInstance().isConnected()) {
            Message.builder()
                    .type(Message.Type.BROADCAST)
                    .payload(Payload.withBroadcast(advertMessage, "/ah view-listing " + listing.getId()))
                    .build()
                    .send(Broker.getInstance());
        }
        return true;
    }
}
