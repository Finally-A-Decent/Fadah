package info.preva1l.fadah.guis;

import com.github.puregero.multilib.MultiLib;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.records.collection.CollectableItem;
import info.preva1l.fadah.records.collection.ExpiredItems;
import info.preva1l.fadah.records.history.HistoricItem;
import info.preva1l.fadah.records.history.History;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.TimeUtil;
import info.preva1l.fadah.utils.guis.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.List;

public class ExpiredListingsMenu extends PaginatedFastInv {
    private final Player viewer;
    private final OfflinePlayer owner;

    public ExpiredListingsMenu(Player viewer, OfflinePlayer owner) {
        super(LayoutManager.MenuType.EXPIRED_LISTINGS.getLayout().guiSize(),
                LayoutManager.MenuType.EXPIRED_LISTINGS.getLayout().formattedTitle(viewer.getUniqueId() == owner.getUniqueId()
                        ? StringUtils.capitalize(Lang.i().getWords().getYour())
                        : owner.getName()+"'s", owner.getName()+"'s"), viewer, LayoutManager.MenuType.EXPIRED_LISTINGS,
                List.of(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34));
        this.viewer = viewer;
        this.owner = owner;

        fillers();
        setPaginationMappings(getLayout().paginationSlots());
        addNavigationButtons();
        fillPaginationItems();
        populatePage();
        addPaginationControls();
    }

    @Override
    protected synchronized void fillPaginationItems() {
        for (CollectableItem expiredItem : CacheAccess.getNotNull(ExpiredItems.class, owner.getUniqueId()).expiredItems()) {
            ItemBuilder itemStack = new ItemBuilder(expiredItem.itemStack().clone())
                    .addLore(getLang().getLore("lore", TimeUtil.formatTimeSince(expiredItem.dateAdded())));

            addPaginationItem(new PaginatedItem(itemStack.build(), e -> {
                MultiLib.getEntityScheduler(viewer).execute(Fadah.getINSTANCE(), () -> {
                    int slot = viewer.getInventory().firstEmpty();
                    if (slot == -1) {
                        Lang.sendMessage(viewer, Lang.i().getPrefix() + Lang.i().getErrors().getInventoryFull());
                        return;
                    }
                    CacheAccess.getNotNull(ExpiredItems.class, owner.getUniqueId()).remove(expiredItem);
                    viewer.getInventory().setItem(slot, expiredItem.itemStack());

                    updatePagination();

                    // In game logs
                    boolean isAdmin = viewer.getUniqueId() != owner.getUniqueId();
                    HistoricItem historicItem = new HistoricItem(owner.getUniqueId(), Instant.now().toEpochMilli(),
                            isAdmin ? HistoricItem.LoggedAction.EXPIRED_ITEM_ADMIN_CLAIM : HistoricItem.LoggedAction.EXPIRED_ITEM_CLAIM,
                            expiredItem.itemStack(), null, null);
                    CacheAccess.getNotNull(History.class, owner.getUniqueId()).add(historicItem);
                },null, 0L);
            }));
        }
    }

    @Override
    protected void updatePagination() {
        super.updatePagination();
    }

    private void addNavigationButtons() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.BACK, -1),
                GuiHelper.constructButton(GuiButtonType.BACK), e ->
                new ProfileMenu(viewer, owner).open(viewer));
    }
}
