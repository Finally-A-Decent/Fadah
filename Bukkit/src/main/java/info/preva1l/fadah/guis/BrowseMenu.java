package info.preva1l.fadah.guis;

import info.preva1l.fadah.Fadah;
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
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created on 19/03/2025
 *
 * @author Preva1l
 */
public abstract class BrowseMenu extends ScrollBarFastInv {
    protected final Supplier<List<Listing>> listingSupplier;
    protected final OfflinePlayer owner;

    // Filters
    protected final String search;
    protected SortingMethod sortingMethod;
    protected SortingDirection sortingDirection;
    protected Category category;

    private final ConcurrentMap<Listing, Boolean> processingListings = new ConcurrentHashMap<>();

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
        this.owner = owner;
        this.search = search;
        this.sortingMethod = (sortingMethod == null ? SortingMethod.AGE : sortingMethod);
        this.sortingDirection = (sortingDirection == null ? SortingDirection.ASCENDING : sortingDirection);
        this.category = category;

        fillers();
        setScrollbarSlots(getLayout().scrollbarSlots());
        setPaginationMappings(getLayout().paginationSlots());

        addNavigationButtons();
        addFilterButtons();

        fillScrollbarItems();
        fillPaginationItems();

        populateScrollbar();
        addScrollbarControls();

        populatePage();
        addPaginationControls();
    }

    private List<Listing> getFilteredListings() {
        List<Listing> listings = listingSupplier.get();

        return listings.stream()
                .filter(this::passesSearchFilter)
                .filter(this::passesCategoryFilter)
                .sorted(sortingMethod.getSorter(sortingDirection))
                .collect(Collectors.toList());
    }

    private boolean passesSearchFilter(Listing listing) {
        if (search == null) return true;
        return Text.doesItemHaveString(search, listing.getItemStack())
                || doesBookHaveEnchant(search, listing.getItemStack());
    }

    private boolean passesCategoryFilter(Listing listing) {
        if (category == null) return true;
        return listing.getCategory().equals(category);
    }

    @Override
    protected void fillPaginationItems() {
        List<Listing> filteredListings = getFilteredListings();

        for (Listing listing : filteredListings) {
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

            addPaginationItem(new PaginatedItem(item, event -> handleListingClick(event, listing, isShulkerBox, isBidListing)));
        }
    }

    private void handleListingClick(InventoryClickEvent event, Listing listing, boolean isShulkerBox, boolean isBidListing) {
        if (processingListings.putIfAbsent(listing, true) != null) return;

        Player clicker = (Player) event.getWhoClicked();

        if (event.isShiftClick() && canCancelListing(clicker, listing)) {
            listing.cancel(clicker).thenRun(() -> {
                updatePagination();
                processingListings.remove(listing);
            });
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

        processingListings.remove(listing);
    }

    private boolean canCancelListing(Player player, Listing listing) {
        return (player.hasPermission("fadah.manage.active-listings") || listing.isOwner(player)) && listing.isActive();
    }

    private ItemStack buildItem(Listing listing, boolean isBidListing, boolean isShulkerBox) {
        Component buyMode = getLang().getStringFormatted(
                isBidListing ? "listing.mode.bidding" : "listing.mode.buy-it-now"
        );

        ItemBuilder itemStack = new ItemBuilder(listing.getItemStack().clone())
                .addLore(getLang().getLore(player, "listing.lore-body",
                        Tuple.of("%seller%", listing.getOwnerName()),
                        Tuple.of("%category%", Text.removeColorCodes(listing.getCategory().name())),
                        Tuple.of("%mode%", buyMode),
                        Tuple.of("%symbol%", listing.getCurrency().getSymbol()),
                        Tuple.of("%price%", Config.i().getFormatting().numbers().format(listing.getPrice())),
                        Tuple.of("%expiry%", TimeUtil.formatTimeUntil(listing.getDeletionDate())),
                        Tuple.of("%currency%", listing.getCurrency().getName())
                ));

        addFooterLore(itemStack, listing, isBidListing, isShulkerBox);
        return itemStack.build();
    }

    private void addFooterLore(ItemBuilder itemStack, Listing listing, boolean isBidListing, boolean isShulkerBox) {
        if (listing.isOwner(player)) {
            itemStack.addLore(getLang().getStringFormatted("listing.footer.own-listing"));
        } else if (listing.getCurrency().canAfford(player, listing.getPrice())) {
            if (isBidListing) {
                itemStack.addLore(getLang().getStringFormatted("listing.footer.bid"));
            } else {
                itemStack.addLore(getLang().getStringFormatted("listing.footer.buy"));
            }
        } else {
            itemStack.addLore(getLang().getStringFormatted("listing.footer.too-expensive"));
        }

        if (isShulkerBox) {
            itemStack.addLore(getLang().getStringFormatted("listing.footer.shulker"));
        }
    }

    protected void addFilterButtons() {
        addSearchButton();
        addFilterTypeButton();
        addFilterDirectionButton();
    }

    private void addSearchButton() {
        removeItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.SEARCH,-1));
        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.SEARCH,-1),
                new ItemBuilder(getLang().getAsMaterial("filter.search.icon", Material.OAK_SIGN))
                        .name(getLang().getStringFormatted("filter.search.name", "&3&lSearch"))
                        .modelData(getLang().getInt("filter.search.model-data"))
                        .lore(getLang().getLore("filter.search.lore")).build(), e ->
                        new InputMenu<>(player, Menus.i().getSearchTitle(), getLang().getString("filter.search.placeholder", "Search Query..."), String.class, search ->
                                new ViewListingsMenu(player, owner, search, sortingMethod, sortingDirection) .open(player)));
    }

    private void addFilterTypeButton() {
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
                        .build(), this::handleFilterTypeClick);
    }

    private void handleFilterTypeClick(InventoryClickEvent e) {
        if (CooldownManager.hasCooldown(CooldownManager.Cooldown.SORT, player)) {
            Lang.sendMessage(player, Lang.i().getPrefix() + Lang.i().getErrors().getCooldown()
                    .replace("%time%", CooldownManager.getCooldownString(CooldownManager.Cooldown.SORT, player)));
            return;
        }

        CooldownManager.startCooldown(CooldownManager.Cooldown.SORT, player);

        if (e.isLeftClick() && sortingMethod.previous() != null) {
            this.sortingMethod = sortingMethod.previous();
            updatePagination();
            addFilterButtons();
        } else if (e.isRightClick() && sortingMethod.next() != null) {
            this.sortingMethod = sortingMethod.next();
            updatePagination();
            addFilterButtons();
        }
    }

    private void addFilterDirectionButton() {
        Component asc = formatDirectionOption(SortingDirection.ASCENDING);
        Component desc = formatDirectionOption(SortingDirection.DESCENDING);

        removeItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.FILTER_DIRECTION,-1));
        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.FILTER_DIRECTION,-1),
                new ItemBuilder(getLang().getAsMaterial("filter.change-direction.icon", Material.CLOCK))
                        .name(getLang().getStringFormatted("filter.change-direction.name", "&eFilter Direction"))
                        .modelData(getLang().getInt("filter.change-direction.model-data"))
                        .lore(getLang().getLore("filter.change-direction.lore",
                                Tuple.of("%first%", asc),
                                Tuple.of("%second%", desc))
                        ).build(), this::handleDirectionToggle);
    }

    private Component formatDirectionOption(SortingDirection direction) {
        String template = sortingDirection == direction
                ? "filter.change-direction.options.selected"
                : "filter.change-direction.options.not-selected";
        String format = sortingDirection == direction ? "&8> &e%option%" : "&f%option%";

        return getLang().getStringFormatted(template, format,
                Tuple.of("%option%", sortingMethod.getLang(direction)));
    }

    private void handleDirectionToggle(InventoryClickEvent e) {
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

    private boolean doesBookHaveEnchant(String enchant, ItemStack enchantedBook) {
        if (!Config.i().getSearch().isEnchantedBooks()) return false;
        if (enchantedBook.getType() != Material.ENCHANTED_BOOK) return false;

        return enchantedBook.getEnchantments().keySet().stream()
                .anyMatch(enchantment -> enchantment.getKey().getKey().toUpperCase().contains(enchant.toUpperCase()));
    }

    protected abstract void addNavigationButtons();

    @Override
    protected void updatePagination() {
        super.updatePagination();
    }
}