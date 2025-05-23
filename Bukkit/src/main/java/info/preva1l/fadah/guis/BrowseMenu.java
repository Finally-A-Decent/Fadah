package info.preva1l.fadah.guis;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Categories;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.config.misc.Tuple;
import info.preva1l.fadah.filters.SortingDirection;
import info.preva1l.fadah.filters.SortingMethod;
import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.records.listing.BidListing;
import info.preva1l.fadah.records.listing.BinListing;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.utils.CooldownManager;
import info.preva1l.fadah.utils.Text;
import info.preva1l.fadah.utils.TimeUtil;
import info.preva1l.fadah.utils.guis.ItemBuilder;
import info.preva1l.fadah.utils.guis.LayoutService;
import info.preva1l.fadah.utils.guis.PaginatedItem;
import info.preva1l.fadah.utils.guis.ScrollBarFastInv;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * Created on 19/03/2025
 *
 * @author Preva1l
 */
public abstract class BrowseMenu extends ScrollBarFastInv {
    protected final Supplier<List<Listing>> listingSupplier;
    protected final List<Listing> listings;

    protected final OfflinePlayer owner;

    // Filters
    protected final String search;
    protected SortingMethod sortingMethod;
    protected SortingDirection sortingDirection;
    protected Category category;

    protected BrowseMenu(
            Player player,
            OfflinePlayer owner,
            LayoutService.MenuType menuType,
            Supplier<List<Listing>> listings,
            @Nullable String search,
            @Nullable SortingMethod sortingMethod,
            @Nullable SortingDirection sortingDirection,
            @Nullable Category category
    ) {
        super(
                menuType.getLayout().guiSize(),
                menuType.getLayout().formattedTitle(
                        Tuple.of("%dynamic%", player.getUniqueId() == owner.getUniqueId()
                                ? Text.capitalizeFirst(Lang.i().getWords().getYour())
                                : owner.getName()),
                        Tuple.of("%username%", owner.getName())
                ),
                player,
                menuType
        );

        this.listingSupplier = listings;
        this.listings = listingSupplier.get();

        this.owner = owner;

        this.search = search;
        this.sortingMethod = (sortingMethod == null ? SortingMethod.AGE : sortingMethod);
        this.sortingDirection = (sortingDirection == null ? SortingDirection.ASCENDING : sortingDirection);
        this.category = category;

        this.listings.sort(this.sortingMethod.getSorter(this.sortingDirection));

        if (search != null) {
            this.listings.removeIf(
                    listing -> !(Text.doesItemHaveString(search, listing.getItemStack())
                            || doesBookHaveEnchant(search, listing.getItemStack()))
            );
        }

        if (category != null) {
            this.listings.removeIf(listing -> !listing.getCategoryID().equals(category.id()));
        }

        fillers();
        setScrollbarSlots(getLayout().scrollbarSlots());
        setPaginationMappings(getLayout().paginationSlots());

        addNavigationButtons();
        addFilterButtons();

        fillScrollbarItems();
        fillPaginationItems();

        populateScrollbar();
        addScrollbarControls();

        // Populate Listings must always be before addPaginationControls!
        populatePage();
        addPaginationControls();
    }

    @Override
    protected void fillPaginationItems() {
        synchronized (listings) {
            for (Listing listing : listings) {
                if (listing.getCurrency() == null) {
                    Fadah.getInstance().getLogger().severe(
                            "Cannot load listing %s because currency %s is not on this server!"
                                    .formatted(listing.getId(), listing.getCurrencyId())
                    );
                    continue;
                }

                boolean isShulkerBox = listing.getItemStack().getType().name().toUpperCase().endsWith("SHULKER_BOX");
                boolean isBidListing = listing instanceof BidListing;

                ItemStack item = buildItem(listing, isBidListing, isShulkerBox);

                addPaginationItem(new PaginatedItem(item, event -> {
                    Player clicker = (Player) event.getWhoClicked();

                    if (event.isShiftClick() && (clicker.hasPermission("fadah.manage.active-listings") || listing.isOwner(clicker))) {
                        if (listing.cancel(clicker)) updatePagination();
                        return;
                    }

                    if (event.isRightClick() && isShulkerBox) {
                        new ShulkerBoxPreviewMenu(listing, () -> open(player)).open(player);
                        return;
                    }

                    if (!listing.canBuy(player)) return;

                    if (isBidListing) {
                        new PlaceBidMenu((BidListing) listing, player, () -> open(player)).open(player);
                    } else {
                        new ConfirmPurchaseMenu((BinListing) listing, player, () -> open(player)).open(player);
                    }
                }));
            }
        }
    }

    private ItemStack buildItem(Listing listing, boolean isBidListing, boolean isShulkerBox) {
        Component buyMode = getLang().getStringFormatted(
                isBidListing ? "listing.mode.bidding" : "listing.mode.buy-it-now"
        );

        ItemBuilder itemStack = new ItemBuilder(listing.getItemStack().clone())
                .addLore(getLang().getLore(player, "listing.lore-body",
                        Tuple.of("%seller%", listing.getOwnerName()),
                        Tuple.of("%category%", Text.removeColorCodes(Categories.getCatName(listing.getCategoryID()))),
                        Tuple.of("%mode%", buyMode),
                        Tuple.of("%price%", Config.i().getFormatting().numbers().format(listing.getPrice())),
                        Tuple.of("%expiry%", TimeUtil.formatTimeUntil(listing.getDeletionDate())),
                        Tuple.of("%currency%", listing.getCurrency().getName())
                ));

        if (listing.isOwner(player))
            itemStack.addLore(getLang().getStringFormatted("listing.footer.own-listing"));
        else if (listing.getCurrency().canAfford(player, listing.getPrice()))
            if (isBidListing) itemStack.addLore(getLang().getStringFormatted("listing.footer.bid"));
            else itemStack.addLore(getLang().getStringFormatted("listing.footer.buy"));
        else
            itemStack.addLore(getLang().getStringFormatted("listing.footer.too-expensive"));

        if (isShulkerBox) itemStack.addLore(getLang().getStringFormatted("listing.footer.shulker"));

        return itemStack.build();
    }

    protected void addFilterButtons() {
        // Search
        removeItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.SEARCH,-1));
        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.SEARCH,-1),
                new ItemBuilder(getLang().getAsMaterial("filter.search.icon", Material.OAK_SIGN))
                        .name(getLang().getStringFormatted("filter.search.name", "&3&lSearch"))
                        .modelData(getLang().getInt("filter.search.model-data"))
                        .lore(getLang().getLore("filter.search.lore")).build(), e ->
                        new InputMenu<>(player, Menus.i().getSearchTitle(), getLang().getString("filter.search.placeholder", "Search Query..."), String.class, search ->
                                new ViewListingsMenu(player, owner, search, sortingMethod, sortingDirection) .open(player)));

        // Filter Type Cycle
        SortingMethod prev = sortingMethod.previous();
        SortingMethod next = sortingMethod.next();
        removeItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.FILTER,-1));
        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.FILTER,-1),
                new ItemBuilder(getLang().getAsMaterial("filter.change-type.icon", Material.PUFFERFISH))
                        .name(getLang().getStringFormatted("filter.change-type.name", "&eListing Filter"))
                        .modelData(getLang().getInt("filter.change-type.model-data"))
                        .addLore(getLang().getLore("filter.change-type.lore",
                                Tuple.of("%previous%", (prev == null ? Lang.i().getWords().getNone() : prev.getFriendlyName())),
                                Tuple.of("%current%", sortingMethod.getFriendlyName()),
                                Tuple.of("%next%", (next == null ? Lang.i().getWords().getNone() : next.getFriendlyName()))))
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
        Component asc =
                sortingDirection == SortingDirection.ASCENDING
                        ? getLang().getStringFormatted(
                        "filter.change-direction.options.selected",
                        "&8> &e%option%",
                        Tuple.of("%option%", sortingMethod.getLang(SortingDirection.ASCENDING))
                )
                        : getLang().getStringFormatted(
                        "filter.change-direction.options.not-selected",
                        "&f%option%",
                        Tuple.of("%option%", sortingMethod.getLang(SortingDirection.ASCENDING))
                );
        Component desc =
                sortingDirection == SortingDirection.DESCENDING
                        ? getLang().getStringFormatted(
                        "filter.change-direction.options.selected",
                        "&8> &e%option%",
                        Tuple.of("%option%", sortingMethod.getLang(SortingDirection.DESCENDING))
                )
                        : getLang().getStringFormatted(
                        "filter.change-direction.options.not-selected",
                        "&f%option%",
                        Tuple.of("%option%", sortingMethod.getLang(SortingDirection.DESCENDING))
                );

        removeItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.FILTER_DIRECTION,-1));
        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.FILTER_DIRECTION,-1),
                new ItemBuilder(getLang().getAsMaterial("filter.change-direction.icon", Material.CLOCK))
                        .name(getLang().getStringFormatted("filter.change-direction.name", "&eFilter Direction"))
                        .modelData(getLang().getInt("filter.change-direction.model-data"))
                        .lore(getLang().getLore("filter.change-direction.lore",
                                Tuple.of("%first%", asc),
                                Tuple.of("%second%", desc))
                        ).build(), e -> {
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

    protected abstract void addNavigationButtons();

    @Override
    protected void updatePagination() {
        synchronized (listings) {
            this.listings.clear();
            this.listings.addAll(listingSupplier.get());

            if (search != null) {
                listings.removeIf(listing -> !(Text.doesItemHaveString(search, listing.getItemStack())
                        || doesBookHaveEnchant(search, listing.getItemStack())));
            }

            if (category != null) {
                this.listings.removeIf(listing -> !listing.getCategoryID().equals(category.id()));
            }

            listings.sort(this.sortingMethod.getSorter(this.sortingDirection));

            super.updatePagination();
        }
    }
}
