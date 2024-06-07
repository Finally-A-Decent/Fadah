package info.preva1l.fadah.guis;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.ExpiredListingsCache;
import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.multiserver.CacheSync;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.TimeUtil;
import info.preva1l.fadah.utils.guis.*;
import info.preva1l.fadah.utils.helpers.TransactionLogger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActiveListingsMenu extends FastInv {
    private static final int maxItemsPerPage = 21;
    private final Player viewer;
    private final OfflinePlayer owner;
    private final int page;
    private final List<Listing> listings;
    private final Map<Integer, Integer> listingSlot = new HashMap<>();
    private int index = 0;

    public ActiveListingsMenu(Player viewer, OfflinePlayer owner, int page) {
        super(45, Menus.ACTIVE_LISTINGS_TITLE.toFormattedString(
                viewer.getUniqueId() == owner.getUniqueId() ?
                        Lang.WORD_YOUR.toCapital() : owner.getName()+"'s", owner.getName()+"'s"));
        this.viewer = viewer;
        this.owner = owner;
        this.page = page;
        this.listings = ListingCache.getListings();
        listings.removeIf(listing -> !listing.isOwner(owner.getUniqueId()));

        fillMappings();

        setItems(getBorders(), GuiHelper.constructButton(GuiButtonType.BORDER));

        addNavigationButtons();
        populateCollectableItems();
        addPaginationControls();

        BukkitTask task = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(Fadah.getINSTANCE(),
                this::refreshMenu, 20L, 20L);
        InventoryEventHandler.tasksToQuit.put(getInventory(), task);
    }

    private void refreshMenu() {
        populateCollectableItems();
        addPaginationControls();
    }

    private void populateCollectableItems() {
        if (listings == null || listings.isEmpty()) {
            setItem(22, new ItemBuilder(Menus.NO_ITEM_FOUND_ICON.toMaterial())
                    .name(Menus.NO_ITEM_FOUND_NAME.toFormattedString()).modelData(
                            Menus.NO_ITEM_FOUND_MODEL_DATA.toInteger()).lore(Menus.NO_ITEM_FOUND_LORE.toLore()).build()
            );
            return;
        }
        for (int i = 0; i <= maxItemsPerPage; i++) {
            index = maxItemsPerPage * page + i;
            if (index >= listings.size() || i == maxItemsPerPage) break;
            Listing listing = listings.get(index);

            ItemBuilder itemStack = new ItemBuilder(listing.getItemStack().clone())
                    .addLore(Menus.ACTIVE_LISTINGS_LORE.toLore(listing.getCategoryID(), listing.getPrice(),
                            TimeUtil.formatTimeUntil(listing.getDeletionDate())));

            removeItem(listingSlot.get(i));
            setItem(listingSlot.get(i), itemStack.build(), e -> handleListingCancellation(listing));
        }
    }

    private void handleListingCancellation(Listing listing) {
        if (ListingCache.getListing(listing.getId()) == null || (Config.STRICT_CHECKS.toBoolean()
                && Fadah.getINSTANCE().getDatabase().getListing(listing.getId()) == null)) {
            viewer.sendMessage(StringUtils.colorize(
                    Lang.PREFIX.toFormattedString() + Lang.DOES_NOT_EXIST.toFormattedString()));
            return;
        }
        viewer.sendMessage(StringUtils.colorize(
                Lang.PREFIX.toFormattedString() + Lang.CANCELLED.toFormattedString()));
        if (Fadah.getINSTANCE().getCacheSync() == null) {
            ListingCache.removeListing(listing);
        }
        CacheSync.send(listing.getId(), true);
        Fadah.getINSTANCE().getDatabase().removeListing(listing.getId());

        CollectableItem collectableItem = new CollectableItem(listing.getItemStack(), Instant.now().toEpochMilli());
        ExpiredListingsCache.addItem(owner.getUniqueId(), collectableItem);
        CacheSync.send(CacheSync.CacheType.EXPIRED_LISTINGS, owner.getUniqueId());

        Fadah.getINSTANCE().getDatabase().addToExpiredItems(listing.getOwner(), collectableItem);
        new ActiveListingsMenu(viewer, owner, page).open(viewer);
        TransactionLogger.listingRemoval(listing, viewer.getUniqueId() != owner.getUniqueId());
    }

    private void addPaginationControls() {
        if (page > 0) {
            setItem(39, GuiHelper.constructButton(GuiButtonType.PREVIOUS_PAGE), e ->
                    new ActiveListingsMenu(viewer, owner,page - 1).open(viewer));
        }
        if (listings != null && listings.size() >= index + 1) {
            setItem(41, GuiHelper.constructButton(GuiButtonType.NEXT_PAGE), e ->
                    new ActiveListingsMenu(viewer, owner,page + 1).open(viewer));
        }
    }

    private void addNavigationButtons() {
        setItem(36, GuiHelper.constructButton(GuiButtonType.BACK), e ->
                new ProfileMenu(viewer, owner).open(viewer));
    }

    private void fillMappings() {
        listingSlot.put(0, 10);
        listingSlot.put(1, 11);
        listingSlot.put(2, 12);
        listingSlot.put(3, 13);
        listingSlot.put(4, 14);
        listingSlot.put(5, 15);
        listingSlot.put(6, 16);
        listingSlot.put(7, 19);
        listingSlot.put(8, 20);
        listingSlot.put(9, 21);
        listingSlot.put(10, 22);
        listingSlot.put(11, 23);
        listingSlot.put(12, 24);
        listingSlot.put(13, 25);
        listingSlot.put(14, 28);
        listingSlot.put(15, 29);
        listingSlot.put(16, 30);
        listingSlot.put(17, 31);
        listingSlot.put(18, 32);
        listingSlot.put(19, 33);
        listingSlot.put(20, 34);
    }
}