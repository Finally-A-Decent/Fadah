package info.preva1l.fadah.guis;

import com.github.puregero.multilib.MultiLib;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.config.misc.Tuple;
import info.preva1l.fadah.records.collection.CollectableItem;
import info.preva1l.fadah.records.collection.CollectionBox;
import info.preva1l.fadah.records.collection.ExpiredItems;
import info.preva1l.fadah.records.history.HistoricItem;
import info.preva1l.fadah.records.history.History;
import info.preva1l.fadah.utils.Text;
import info.preva1l.fadah.utils.TimeUtil;
import info.preva1l.fadah.utils.guis.ItemBuilder;
import info.preva1l.fadah.utils.guis.LayoutManager;
import info.preva1l.fadah.utils.guis.PaginatedFastInv;
import info.preva1l.fadah.utils.guis.PaginatedItem;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.List;

/**
 * Created on 19/03/2025
 *
 * @author Preva1l
 */
public class CollectionMenu extends PaginatedFastInv {
    protected final OfflinePlayer owner;
    protected final boolean expired;

    private final List<CollectableItem> items;

    public CollectionMenu(Player viewer, OfflinePlayer owner, LayoutManager.MenuType menuType) {
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
        this.expired = menuType == LayoutManager.MenuType.EXPIRED_LISTINGS;

        this.items = expired
                ? CacheAccess.getNotNull(ExpiredItems.class, owner.getUniqueId()).expiredItems()
                : CacheAccess.getNotNull(CollectionBox.class, owner.getUniqueId()).collectableItems();

        this.items.sort(CollectableItem::compareTo);

        fillers();
        setPaginationMappings(getLayout().paginationSlots());
        addNavigationButtons();
        fillPaginationItems();
        populatePage();
        addPaginationControls();
    }

    @Override
    protected void fillPaginationItems() {
        for (CollectableItem expiredItem : items) {
            ItemBuilder itemStack = new ItemBuilder(expiredItem.itemStack().clone())
                    .addLore(getLang().getLore("lore", Tuple.of("%time%", TimeUtil.formatTimeSince(expiredItem.dateAdded()))));

            addPaginationItem(new PaginatedItem(itemStack.build(), e -> {
                MultiLib.getEntityScheduler(player).execute(Fadah.getInstance(), () -> {
                    int slot = player.getInventory().firstEmpty();
                    if (slot == -1) {
                        Lang.sendMessage(player, Lang.i().getPrefix() + Lang.i().getErrors().getInventoryFull());
                        return;
                    }
                    removeFromCache(expiredItem);
                    player.getInventory().setItem(slot, expiredItem.itemStack());

                    updatePagination();

                    // In game logs
                    boolean isAdmin = player.getUniqueId() != owner.getUniqueId();
                    HistoricItem.LoggedAction action = expired
                            ? isAdmin ? HistoricItem.LoggedAction.EXPIRED_ITEM_ADMIN_CLAIM : HistoricItem.LoggedAction.EXPIRED_ITEM_CLAIM
                            : isAdmin ? HistoricItem.LoggedAction.COLLECTION_BOX_ADMIN_CLAIM : HistoricItem.LoggedAction.COLLECTION_BOX_CLAIM;
                    HistoricItem historicItem = new HistoricItem(
                            Instant.now().toEpochMilli(),
                            action,
                            expiredItem.itemStack(),
                            null,
                            null,
                            null);
                    CacheAccess.getNotNull(History.class, owner.getUniqueId()).add(historicItem);
                }, null, 0L);
            }));

        }
    }

    private void removeFromCache(CollectableItem item) {
        if (expired) {
            CacheAccess.getNotNull(ExpiredItems.class, owner.getUniqueId()).remove(item);
        } else {
            CacheAccess.getNotNull(CollectionBox.class, owner.getUniqueId()).remove(item);
        }
    }

    protected void addNavigationButtons() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.BACK, -1),
                Menus.i().getBackButton().itemStack(), e ->
                        new ProfileMenu(player, owner).open(player));
    }

    @Override
    protected void updatePagination() {
        this.items.sort(CollectableItem::compareTo);

        super.updatePagination();
    }
}
