package info.preva1l.fadah.guis;

import com.github.puregero.multilib.MultiLib;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.api.BukkitListing;
import info.preva1l.fadah.cache.CategoryCache;
import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.data.PermissionsData;
import info.preva1l.fadah.multiserver.CacheSync;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.TimeUtil;
import info.preva1l.fadah.utils.guis.*;
import info.preva1l.fadah.utils.helpers.TransactionLogger;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class NewListingMenu extends FastInv {
    private final Fadah plugin = Fadah.getINSTANCE();
    private final Player player;
    private ItemStack itemToSell;
    private Instant timeToDelete;
    private boolean listingStarted = false;

    public NewListingMenu(Player player, double price) {
        super(54, Menus.NEW_LISTING_TITLE.toFormattedString(), LayoutManager.MenuType.NEW_LISTING);
        this.player = player;
        this.itemToSell = player.getInventory().getItemInMainHand().clone();
        MultiLib.getEntityScheduler(player).execute(plugin,
                () -> player.getInventory().setItemInMainHand(new ItemStack(Material.AIR)),
                () -> this.itemToSell = new ItemStack(Material.AIR),
                0L);
        this.timeToDelete = Instant.now().plus(6, ChronoUnit.HOURS);

        setItems(getBorders(), GuiHelper.constructButton(GuiButtonType.BORDER));

        setItem(30, new ItemBuilder(Menus.NEW_LISTING_CREATE_ICON.toMaterial()).name(Menus.NEW_LISTING_CREATE_NAME.toFormattedString())
                .modelData(Menus.NEW_LISTING_CREATE_MODEL_DATA.toInteger())
                .addLore(Menus.NEW_LISTING_CREATE_LORE.toLore(new DecimalFormat(Config.DECIMAL_FORMAT.toString()).format(price))).build(), e -> startListing(timeToDelete, price));
        setClock();

        addNavigationButtons();
        setItem(22, itemToSell);
    }

    @Override
    protected void onClose(InventoryCloseEvent event) {
        super.onClose(event);
        if (!listingStarted) player.getInventory().setItemInMainHand(itemToSell);
    }

    private void setClock() {
        removeItem(32);
        setItem(32, new ItemBuilder(Menus.NEW_LISTING_TIME_ICON.toMaterial()).name(Menus.NEW_LISTING_TIME_NAME.toFormattedString())
                .addLore(Menus.NEW_LISTING_TIME_LORE.toLore(TimeUtil.formatTimeUntil(timeToDelete.toEpochMilli()))).build(), e -> {
            if (e.isRightClick()) {
                if (e.isShiftClick()) {
                    if (timeToDelete.minus(30, ChronoUnit.MINUTES).toEpochMilli() <= Instant.now().toEpochMilli())
                        return;
                    timeToDelete = timeToDelete.minus(30, ChronoUnit.MINUTES);
                    setClock();
                    return;
                }
                if (timeToDelete.minus(1, ChronoUnit.HOURS).toEpochMilli() <= Instant.now().toEpochMilli()) return;
                timeToDelete = timeToDelete.minus(1, ChronoUnit.HOURS);
                setClock();
            }

            if (e.isLeftClick()) {
                if (e.isShiftClick()) {
                    if (timeToDelete.plus(30, ChronoUnit.MINUTES).toEpochMilli() > Instant.now().plus(10, ChronoUnit.DAYS).toEpochMilli())
                        return;
                    timeToDelete = timeToDelete.plus(30, ChronoUnit.MINUTES);
                    setClock();
                    return;
                }
                if (timeToDelete.plus(1, ChronoUnit.HOURS).toEpochMilli() > Instant.now().plus(10, ChronoUnit.DAYS).toEpochMilli())
                    return;
                timeToDelete = timeToDelete.plus(1, ChronoUnit.HOURS);
                setClock();
            }
        });
    }

    private void startListing(Instant deletionDate, double price) {
        String category = CategoryCache.getCategoryForItem(itemToSell);

        if (category == null) {
            player.sendMessage(Lang.CANT_SELL.toFormattedString());
            return;
        }

        double tax = PermissionsData.getHighestDouble(PermissionsData.PermissionType.LISTING_TAX, player);

        Listing listing = new BukkitListing(UUID.randomUUID(), player.getUniqueId(), player.getName(),
                itemToSell, category, price, tax, Instant.now().toEpochMilli(), deletionDate.toEpochMilli());

        Fadah.getINSTANCE().getDatabase().addListing(listing);
        if (Fadah.getINSTANCE().getCacheSync() != null) {
            CacheSync.send(listing.getId(), false);
        } else {
            ListingCache.addListing(listing);
        }

        listingStarted = true;

        player.closeInventory();

        double taxAmount = PermissionsData.getHighestDouble(PermissionsData.PermissionType.LISTING_TAX, player);
        String itemname = listing.getItemStack().getItemMeta().getDisplayName().isBlank() ? listing.getItemStack().getType().name() : listing.getItemStack().getItemMeta().getDisplayName();
        String message = String.join("\n", Lang.NOTIFICATION_NEW_LISTING.toLore(itemname,
                new DecimalFormat(Config.DECIMAL_FORMAT.toString()).format(listing.getPrice()),
                TimeUtil.formatTimeUntil(listing.getDeletionDate()), PermissionsData.getCurrentListings(player),
                PermissionsData.getHighestInt(PermissionsData.PermissionType.MAX_LISTINGS, player),
                taxAmount, (taxAmount/100) * price));
        player.sendMessage(message);

        TransactionLogger.listingCreated(listing);
    }

    private void addNavigationButtons() {
        setItem(49, GuiHelper.constructButton(GuiButtonType.CLOSE), e -> e.getWhoClicked().closeInventory());
    }
}
