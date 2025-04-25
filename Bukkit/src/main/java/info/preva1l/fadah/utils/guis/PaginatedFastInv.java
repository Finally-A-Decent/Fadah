package info.preva1l.fadah.utils.guis;

import com.github.puregero.multilib.regionized.RegionizedTask;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.utils.TaskManager;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class PaginatedFastInv extends FastInv {
    protected final Player player;

    protected int page = 0;
    protected int index = 0;
    private List<Integer> paginationMappings;
    private final List<PaginatedItem> paginatedItems = new ArrayList<>();
    protected boolean needsClearing = false;
    private final RegionizedTask updateTask;

    protected PaginatedFastInv(int size, @NotNull String title, @NotNull Player player, LayoutManager.MenuType menuType) {
        super(size, title, menuType);
        this.player = player;
        this.paginationMappings = List.of(
                11, 12, 13, 14, 15, 16, 20,
                21, 22, 23, 24, 25, 29, 30,
                31, 32, 33, 34, 38, 39, 40,
                41, 42, 43);

        updateTask = TaskManager.Async.runTask(Fadah.getInstance(), this::updatePagination, 20L, 20L);
        addCloseHandler(event -> updateTask.cancel());
    }

    protected PaginatedFastInv(int size, @NotNull Component title, @NotNull Player player, LayoutManager.MenuType menuType) {
        super(size, title, menuType);
        this.player = player;
        this.paginationMappings = List.of(
                11, 12, 13, 14, 15, 16, 20,
                21, 22, 23, 24, 25, 29, 30,
                31, 32, 33, 34, 38, 39, 40,
                41, 42, 43);

        updateTask = TaskManager.Async.runTask(Fadah.getInstance(), this::updatePagination, 20L, 20L);
        addCloseHandler(event -> updateTask.cancel());
    }

    protected PaginatedFastInv(int size, @NotNull Component title, @NotNull Player player, LayoutManager.MenuType menuType, @NotNull List<Integer> paginationMappings) {
        super(size, title, menuType);
        this.player = player;
        this.paginationMappings = paginationMappings;

        updateTask = TaskManager.Async.runTask(Fadah.getInstance(), this::updatePagination, 20L, 20L);
        addCloseHandler(event -> updateTask.cancel());
    }

    protected void setPaginationMappings(List<Integer> list) {
        this.paginationMappings = list;
    }

    protected void nextPage() {
        if (paginatedItems == null || paginatedItems.size() < index + 1) {
            return;
        }
        page++;
        populatePage();
        addPaginationControls();
    }

    protected void previousPage() {
        if (page == 0) {
            return;
        }
        page--;
        populatePage();
        addPaginationControls();
    }

    protected void populatePage() {
        int maxItemsPerPage = paginationMappings.size();
        boolean empty = paginatedItems == null || paginatedItems.isEmpty();
        if (empty) {
            if (needsClearing) {
                for (Integer paginationMapping : paginationMappings) removeItem(paginationMapping);
                needsClearing = false;
            }
            paginationEmpty();
            return;
        }

        needsClearing = true;
        
        for (int i = 0; i < maxItemsPerPage; i++) {
            removeItem(paginationMappings.get(i));
            index = maxItemsPerPage * page + i;
            if (index >= paginatedItems.size()) continue;
            PaginatedItem item = paginatedItems.get(index);
            setItem(paginationMappings.get(i), item.itemStack(), item.eventConsumer());
        }
    }

    protected void updatePagination() {
        paginatedItems.clear();
        fillPaginationItems();
        populatePage();
        addPaginationControls();
    }

    protected void paginationEmpty() {
        List<Integer> noItems = getLayout().noItems();
        if (!noItems.isEmpty()) {
            setItems(noItems.stream().mapToInt(Integer::intValue).toArray(), Menus.i().getNoItemFound().itemStack());
        }
    }

    protected abstract void fillPaginationItems();

    protected void addPaginationControls() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PAGINATION_CONTROL_ONE, -1),
                Menus.i().getBorder().itemStack());
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PAGINATION_CONTROL_TWO,-1),
                Menus.i().getBorder().itemStack());
        if (page > 0) {
            setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PAGINATION_CONTROL_ONE, -1),
                    Menus.i().getPreviousButton().itemStack(), e -> previousPage());
        }

        if (paginatedItems != null && paginatedItems.size() >= index + 1) {
            setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PAGINATION_CONTROL_TWO,-1),
                    Menus.i().getNextButton().itemStack(), e -> nextPage());
        }
    }

    protected void addPaginationItem(PaginatedItem item) {
        paginatedItems.add(item);
    }
}
