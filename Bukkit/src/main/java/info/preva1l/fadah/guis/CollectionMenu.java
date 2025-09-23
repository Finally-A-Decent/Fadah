package info.preva1l.fadah.guis;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.config.misc.Tuple;
import info.preva1l.fadah.records.StorageHolder;
import info.preva1l.fadah.records.collection.CollectableItem;
import info.preva1l.fadah.records.collection.CollectionBox;
import info.preva1l.fadah.records.collection.ExpiredItems;
import info.preva1l.fadah.records.history.HistoricItem;
import info.preva1l.fadah.records.history.History;
import info.preva1l.fadah.security.AwareDataService;
import info.preva1l.fadah.utils.Tasks;
import info.preva1l.fadah.utils.Text;
import info.preva1l.fadah.utils.TimeUtil;
import info.preva1l.fadah.utils.guis.ItemBuilder;
import info.preva1l.fadah.utils.guis.LayoutService;
import info.preva1l.fadah.utils.guis.PaginatedFastInv;
import info.preva1l.fadah.utils.guis.PaginatedItem;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created on 19/03/2025
 *
 * @author Preva1l
 */
public class CollectionMenu extends PaginatedFastInv {
    protected final OfflinePlayer owner;
    protected final boolean expired;
    private final ConcurrentMap<CollectableItem, Boolean> processingItems = new ConcurrentHashMap<>();

    public CollectionMenu(Player viewer, OfflinePlayer owner, LayoutService.MenuType menuType) {
        super(
                menuType.getLayout().guiSize(),
                menuType.getLayout().formattedTitle(
                        Tuple.of("%dynamic%", viewer.getUniqueId() == owner.getUniqueId()
                                ? Text.capitalizeFirst(Lang.i().getWords().getYour())
                                : owner.getName()),
                        Tuple.of("%username%", owner.getName())),
                viewer,
                menuType,
                List.of(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34)
        );

        this.owner = owner;
        this.expired = menuType == LayoutService.MenuType.EXPIRED_LISTINGS;

        fillers();
        setPaginationMappings(getLayout().paginationSlots());
        addNavigationButtons();
        fillPaginationItems();
        populatePage();
        addPaginationControls();
    }

    private StorageHolder<CollectableItem> getCurrentItems() {
        return expired
                ? CacheAccess.getNotNull(ExpiredItems.class, owner.getUniqueId())
                : CacheAccess.getNotNull(CollectionBox.class, owner.getUniqueId());
    }

    @Override
    protected void fillPaginationItems() {
        List<CollectableItem> items = getCurrentItems().items();
        items.sort(CollectableItem::compareTo);

        for (CollectableItem item : items) {
            ItemBuilder itemStack = new ItemBuilder(item.itemStack().clone())
                    .addLore(getLang().getLore("lore", Tuple.of("%time%", TimeUtil.formatTimeSince(item.dateAdded()))));

            addPaginationItem(new PaginatedItem(itemStack.build(), e -> handleItemClick(item)));
        }
    }

    private void handleItemClick(CollectableItem item) {
        if (processingItems.putIfAbsent(item, true) != null) return;

        try {
            executeSafely(item, () -> claimItem(item));
        } finally {
            processingItems.remove(item);
        }
    }

    private void claimItem(CollectableItem item) {
        List<CollectableItem> currentItems = getCurrentItems().items();
        if (!currentItems.contains(item)) return;

        Tasks.sync(Fadah.getInstance(), player, () -> {
            if (!getCurrentItems().contains(item)) return;

            ItemStack itemStack = item.itemStack();
            if (!tryAddToInventory(player, itemStack)) {
                Lang.sendMessage(player, Lang.i().getPrefix() + Lang.i().getErrors().getInventoryFull());
                return;
            }

            getCurrentItems().remove(item);
            logItemClaim(item);
            updatePagination();
        }, () -> Lang.sendMessage(player, Lang.i().getPrefix() + Lang.i().getErrors().getOther().replace("%ex%", "Ticking Issue")));
    }

    private boolean tryAddToInventory(Player player, ItemStack item) {
        int slot = player.getInventory().firstEmpty();
        if (slot == -1) return false;

        if (player.getInventory().getItem(slot) != null) return false;

        player.getInventory().setItem(slot, item);
        return true;
    }

    private void logItemClaim(CollectableItem item) {
        boolean isAdmin = player.getUniqueId() != owner.getUniqueId();
        HistoricItem.LoggedAction action = expired
                ? isAdmin ? HistoricItem.LoggedAction.EXPIRED_ITEM_ADMIN_CLAIM : HistoricItem.LoggedAction.EXPIRED_ITEM_CLAIM
                : isAdmin ? HistoricItem.LoggedAction.COLLECTION_BOX_ADMIN_CLAIM : HistoricItem.LoggedAction.COLLECTION_BOX_CLAIM;

        HistoricItem historicItem = new HistoricItem(
                Instant.now().toEpochMilli(),
                action,
                item.itemStack(),
                null,
                null,
                false,
                null
        );

        CacheAccess.getNotNull(History.class, owner.getUniqueId()).add(historicItem);
    }

    private void executeSafely(CollectableItem item, Runnable action) {
        try {
            if (expired) {
                var items = CacheAccess.getNotNull(ExpiredItems.class, owner.getUniqueId());
                AwareDataService.instance.execute(ExpiredItems.class, items, item, action);
            } else {
                var items = CacheAccess.getNotNull(CollectionBox.class, owner.getUniqueId());
                AwareDataService.instance.execute(CollectionBox.class, items, item, action);
            }
        } catch (Exception e) {
            Logger.getLogger("Fadah").log(Level.SEVERE, "Issue in collection menu", e);
        }
    }

    protected void addNavigationButtons() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.BACK, -1),
                Menus.i().getBackButton().itemStack(), e ->
                        new ProfileMenu(player, owner).open(player));
    }
}