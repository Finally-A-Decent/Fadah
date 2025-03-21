package info.preva1l.fadah.guis;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CategoryRegistry;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.filters.SortingDirection;
import info.preva1l.fadah.filters.SortingMethod;
import info.preva1l.fadah.records.listing.BidListing;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.utils.CooldownManager;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.TimeUtil;
import info.preva1l.fadah.utils.guis.*;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Created on 19/03/2025
 *
 * @author Preva1l
 */
public abstract class PurchaseMenu extends ScrollBarFastInv {
    protected final Supplier<List<Listing>> listingSupplier;
    protected final List<Listing> listings;
    protected final Lock lock = new ReentrantLock();

    // Filters
    protected final String search;
    protected SortingMethod sortingMethod;
    protected SortingDirection sortingDirection;

    protected PurchaseMenu(
            Player player,
            String title,
            LayoutManager.MenuType menuType,
            Supplier<List<Listing>> listings,
            @Nullable String search,
            @Nullable SortingMethod sortingMethod,
            @Nullable SortingDirection sortingDirection
    ) {
        super(menuType.getLayout().guiSize(), title, player, menuType);

        this.listingSupplier = listings;
        this.listings = listingSupplier.get();

        this.search = search;
        this.sortingMethod = (sortingMethod == null ? SortingMethod.AGE : sortingMethod);
        this.sortingDirection = (sortingDirection == null ? SortingDirection.ASCENDING : sortingDirection);

        lock.lock();
        this.listings.sort(this.sortingMethod.getSorter(this.sortingDirection));

        if (search != null) {
            this.listings.removeIf(
                    listing -> !(StringUtils.doesItemHaveString(search, listing.getItemStack())
                            || doesBookHaveEnchant(search, listing.getItemStack()))
            );
        }

        lock.unlock();

        fillers();
        setScrollbarSlots(getLayout().scrollbarSlots());
        setPaginationMappings(getLayout().paginationSlots());

        addNavigationButtons();
        addFilterButtons();

        fillScrollbarItems();
        fillPaginationItems();

        populateScrollbar();

        // Populate Listings must always be before addPaginationControls!
        populatePage();
        addPaginationControls();
    }

    @Override
    protected synchronized void fillPaginationItems() {
        lock.lock();
        for (Listing listing : listings) {
            if (listing.getCurrency() == null) {
                Fadah.getConsole().severe("Cannot load listing %s because currency %s is not on this server!".formatted(listing.getId(), listing.getCurrencyId()));
                continue;
            }
            String buyMode = listing instanceof BidListing
                    ? getLang().getStringFormatted("listing.lore-buy.bidding")
                    : getLang().getStringFormatted("listing.lore-buy.buy-it-now");

            ItemBuilder itemStack = new ItemBuilder(listing.getItemStack().clone())
                    .addLore(getLang().getLore(player, "listing.lore-body",
                            listing.getOwnerName(),
                            StringUtils.removeColorCodes(CategoryRegistry.getCatName(listing.getCategoryID())), buyMode,
                            new DecimalFormat(Config.i().getFormatting().getNumbers())
                                    .format(listing.getPrice()), TimeUtil.formatTimeUntil(listing.getDeletionDate()),
                            listing.getCurrency().getName()));

            if (player.getUniqueId().equals(listing.getOwner())) {
                itemStack.addLore(getLang().getStringFormatted("listing.lore-footer.own-listing"));
            } else if (listing.getCurrency().canAfford(player, listing.getPrice())) {
                itemStack.addLore(getLang().getStringFormatted("listing.lore-footer.buy"));
            } else {
                itemStack.addLore(getLang().getStringFormatted("listing.lore-footer.too-expensive"));
            }
            if (listing.getItemStack().getType().name().toUpperCase().endsWith("SHULKER_BOX")) {
                itemStack.addLore(getLang().getStringFormatted("listing.lore-footer.is-shulker"));
            }

            addPaginationItem(new PaginatedItem(itemStack.build(), e -> {
                if (e.isShiftClick() && (e.getWhoClicked().hasPermission("fadah.manage.active-listings") || listing.isOwner(((Player) e.getWhoClicked())))) {
                    if (listing.cancel(((Player) e.getWhoClicked()))) {
                        updatePagination();
                    }
                    return;
                }

                if (e.isRightClick() && listing.getItemStack().getType().name().toUpperCase().endsWith("SHULKER_BOX")) {
                    new ShulkerBoxPreviewMenu(listing, player, () -> open(player)).open(player);
                    return;
                }

                if (!listing.canBuy(player)) return;

                new ConfirmPurchaseMenu(listing, player, () -> open(player)).open(player);
            }));
        }
        lock.unlock();
    }

    protected void addNavigationButtons() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.SCROLLBAR_CONTROL_ONE,-1),
                GuiHelper.constructButton(GuiButtonType.SCROLL_PREVIOUS), e -> scrollUp());
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.SCROLLBAR_CONTROL_TWO,-1),
                GuiHelper.constructButton(GuiButtonType.SCROLL_NEXT), e -> scrollDown());
    }

    protected void addFilterButtons() {
        // Filter Type Cycle
        SortingMethod prev = sortingMethod.previous();
        SortingMethod next = sortingMethod.next();
        removeItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.FILTER,-1));
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.FILTER,-1),
                new ItemBuilder(getLang().getAsMaterial("filter.change-type.icon", Material.PUFFERFISH))
                        .name(getLang().getStringFormatted("filter.change-type.name", "&eListing Filter"))
                        .modelData(getLang().getInt("filter.change-type.model-data"))
                        .addLore(getLang().getLore("filter.change-type.lore", (prev == null ? Lang.i().getWords().getNone() : prev.getFriendlyName()),
                                sortingMethod.getFriendlyName(), (next == null ? Lang.i().getWords().getNone() : next.getFriendlyName())))
                        .build(), e -> {
                    if (CooldownManager.hasCooldown(CooldownManager.Cooldown.SORT, player)) {
                        Lang.sendMessage(player, Lang.i().getPrefix() + Lang.i().getErrors().getCooldown()
                                .replace("%time%", CooldownManager.getCooldownString(CooldownManager.Cooldown.SORT, player)));
                        return;
                    }
                    CooldownManager.startCooldown(CooldownManager.Cooldown.SORT, player);
                    if (e.isLeftClick()) {
                        if (sortingMethod.previous() == null) return;
                        this.sortingMethod = sortingMethod.previous();
                        updatePagination();
                        addFilterButtons();
                    }
                    if (e.isRightClick()) {
                        if (sortingMethod.next() == null) return;
                        this.sortingMethod = sortingMethod.next();
                        updatePagination();
                        addFilterButtons();
                    }
                });

        // Filter Direction Toggle
        String asc = StringUtils.formatPlaceholders(sortingDirection == SortingDirection.ASCENDING
                        ? getLang().getStringFormatted("filter.change-direction.options.selected", "&8> &e{0}")
                        : getLang().getStringFormatted("filter.change-direction.options.not-selected", "&f{0}"),
                sortingMethod.getLang(SortingDirection.ASCENDING));
        String desc = StringUtils.formatPlaceholders(sortingDirection == SortingDirection.DESCENDING
                        ? getLang().getStringFormatted("filter.change-direction.options.selected", "&8> &e{0}")
                        : getLang().getStringFormatted("filter.change-direction.options.not-selected", "&f{0}"),
                sortingMethod.getLang(SortingDirection.DESCENDING));

        removeItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.FILTER_DIRECTION,-1));
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.FILTER_DIRECTION,-1),
                new ItemBuilder(getLang().getAsMaterial("filter.change-direction.icon", Material.CLOCK))
                        .name(getLang().getStringFormatted("filter.change-direction.name", "&eFilter Direction"))
                        .modelData(getLang().getInt("filter.change-direction.model-data"))
                        .lore(getLang().getLore("filter.change-direction.lore", asc, desc)).build(), e -> {
                    if (CooldownManager.hasCooldown(CooldownManager.Cooldown.SORT, player)) {
                        Lang.sendMessage(player, Lang.i().getPrefix() + Lang.i().getErrors().getCooldown()
                                .replace("%time%", CooldownManager.getCooldownString(CooldownManager.Cooldown.SORT, player)));
                        return;
                    }
                    CooldownManager.startCooldown(CooldownManager.Cooldown.SORT, player);
                    this.sortingDirection = sortingDirection == SortingDirection.ASCENDING
                            ? SortingDirection.DESCENDING
                            : SortingDirection.ASCENDING;
                    updatePagination();
                    addFilterButtons();
                }
        );
    }

    /**
     * @return true if the item contains the search
     */
    private boolean doesBookHaveEnchant(String enchant, ItemStack enchantedBook) {
        if (!Config.i().getSearch().isEnchantedBooks()) return false;
        if (enchantedBook.getType() == Material.ENCHANTED_BOOK) {
            for (Enchantment enchantment : enchantedBook.getEnchantments().keySet()) {
                if (enchantment.getKey().getKey().toUpperCase().contains(enchant.toUpperCase())) return true;
            }
        }
        return false;
    }

    @Override
    protected void updatePagination() {
        this.listings.clear();
        this.listings.addAll(listingSupplier.get());

        if (search != null) {
            listings.removeIf(listing -> !(StringUtils.doesItemHaveString(search, listing.getItemStack())
                    || doesBookHaveEnchant(search, listing.getItemStack())));
        }

        listings.sort(this.sortingMethod.getSorter(this.sortingDirection));

        super.updatePagination();
    }
}
