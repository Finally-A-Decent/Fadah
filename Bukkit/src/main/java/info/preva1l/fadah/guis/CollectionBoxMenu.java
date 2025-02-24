package info.preva1l.fadah.guis;

import com.github.puregero.multilib.MultiLib;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.cache.HistoricItemsCache;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.CollectionBox;
import info.preva1l.fadah.records.HistoricItem;
import info.preva1l.fadah.records.History;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.TimeUtil;
import info.preva1l.fadah.utils.guis.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.List;

public class CollectionBoxMenu extends PaginatedFastInv {
    private final Player viewer;
    private final OfflinePlayer owner;
    private CollectionBox collectionBox;

    public CollectionBoxMenu(Player viewer, OfflinePlayer owner) {
        super(LayoutManager.MenuType.COLLECTION_BOX.getLayout().guiSize(),
                LayoutManager.MenuType.COLLECTION_BOX.getLayout().formattedTitle(viewer.getUniqueId() == owner.getUniqueId()
                        ? StringUtils.capitalize(Lang.i().getWords().getYour())
                        : owner.getName()+"'s", owner.getName()+"'s"), viewer, LayoutManager.MenuType.COLLECTION_BOX,
                List.of(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34));
        this.viewer = viewer;
        this.owner = owner;
        this.collectionBox = CacheAccess.get(CollectionBox.class, owner.getUniqueId());

        List<Integer> fillerSlots = getLayout().fillerSlots();
        if (!fillerSlots.isEmpty()) {
            setItems(fillerSlots.stream().mapToInt(Integer::intValue).toArray(),
                    GuiHelper.constructButton(GuiButtonType.BORDER));
        }
        setPaginationMappings(getLayout().paginationSlots());

        addNavigationButtons();
        fillPaginationItems();
        populatePage();
        addPaginationControls();
    }

    @Override
    protected synchronized void fillPaginationItems() {
        for (CollectableItem collectableItem : collectionBox.collectableItems()) {
            ItemBuilder itemBuilder = new ItemBuilder(collectableItem.itemStack().clone())
                    .lore(getLang().getLore("lore", TimeUtil.formatTimeSince(collectableItem.dateAdded())));

            addPaginationItem(new PaginatedItem(itemBuilder.build(), e -> {
                MultiLib.getEntityScheduler(viewer).execute(Fadah.getINSTANCE(), () -> {
                    int slot = viewer.getInventory().firstEmpty();
                    if (slot == -1) {
                        Lang.sendMessage(viewer, Lang.i().getPrefix() + Lang.i().getErrors().getInventoryFull());
                        return;
                    }
                    collectionBox.remove(collectableItem);
                    CacheAccess.add(CollectionBox.class, collectionBox);
                    DatabaseManager.getInstance().save(CollectionBox.class, collectionBox);
                    viewer.getInventory().setItem(slot, collectableItem.itemStack());

                    updatePagination();

                    // In game logs
                    boolean isAdmin = viewer.getUniqueId() != owner.getUniqueId();
                    HistoricItem historicItem = new HistoricItem(owner.getUniqueId(), Instant.now().toEpochMilli(),
                            isAdmin ? HistoricItem.LoggedAction.COLLECTION_BOX_ADMIN_CLAIM
                                    : HistoricItem.LoggedAction.COLLECTION_BOX_CLAIM,
                            collectableItem.itemStack(), null, null);
                    HistoricItemsCache.addLog(owner.getUniqueId(), historicItem);
                    DatabaseManager.getInstance().save(History.class, History.of(owner.getUniqueId()));
                }, null, 0L);
            }));
        }
    }

    @Override
    protected void addPaginationControls() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PAGINATION_CONTROL_ONE, -1),
                GuiHelper.constructButton(GuiButtonType.BORDER));
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PAGINATION_CONTROL_TWO,-1),
                GuiHelper.constructButton(GuiButtonType.BORDER));
        if (page > 0) {
            setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PAGINATION_CONTROL_ONE, -1),
                    GuiHelper.constructButton(GuiButtonType.PREVIOUS_PAGE), e -> previousPage());
        }

        if (collectionBox != null && collectionBox.collectableItems().size() >= index + 1) {
            setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PAGINATION_CONTROL_TWO,-1),
                    GuiHelper.constructButton(GuiButtonType.NEXT_PAGE), e -> nextPage());
        }
    }

    @Override
    protected void updatePagination() {
        this.collectionBox = CacheAccess.get(CollectionBox.class, owner.getUniqueId());
        super.updatePagination();
    }

    private void addNavigationButtons() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.BACK, -1),
                GuiHelper.constructButton(GuiButtonType.BACK), e -> new ProfileMenu(viewer, owner).open(viewer));
    }
}
