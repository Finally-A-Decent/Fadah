package info.preva1l.fadah.utils.guis;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.utils.config.BasicConfig;
import info.preva1l.fadah.utils.config.LanguageConfig;
import info.preva1l.trashcan.plugin.annotations.PluginEnable;
import info.preva1l.trashcan.plugin.annotations.PluginReload;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class LayoutManager {
    public static final LayoutManager instance = new LayoutManager();

    private final List<GuiLayout> guiLayouts = new ArrayList<>();

    @PluginReload
    public void reload() {
        Stream.of(
                LayoutManager.MenuType.MAIN,
                LayoutManager.MenuType.NEW_LISTING,
                LayoutManager.MenuType.PROFILE,
                LayoutManager.MenuType.EXPIRED_LISTINGS,
                LayoutManager.MenuType.COLLECTION_BOX,
                LayoutManager.MenuType.CONFIRM_PURCHASE,
                LayoutManager.MenuType.HISTORY,
                LayoutManager.MenuType.WATCH
        ).forEach(this::reloadLayout);
    }

    @PluginEnable
    public void load() {
        var plugin = Fadah.getInstance();
        Stream.of(
                new BasicConfig(plugin, "menus/main.yml"),
                new BasicConfig(plugin, "menus/new-listing.yml"),
                new BasicConfig(plugin, "menus/expired-listings.yml"),
                new BasicConfig(plugin, "menus/historic-items.yml"),
                new BasicConfig(plugin, "menus/confirm.yml"),
                new BasicConfig(plugin, "menus/collection-box.yml"),
                new BasicConfig(plugin, "menus/profile.yml"),
                new BasicConfig(plugin, "menus/view-listings.yml"),
                new BasicConfig(plugin, "menus/watch.yml")
        ).forEach(LayoutManager.instance::loadLayout);
    }

    public void loadLayout(BasicConfig config) {
        final MenuType menuType = getMenuType(config.getFileName());

        final String guiTitle = config.getString("title");

        final List<Integer> fillerSlots = new ArrayList<>();
        final List<Integer> paginationSlots = new ArrayList<>();
        final List<Integer> scrollbarSlots = new ArrayList<>();
        final List<Integer> noItems = new ArrayList<>();
        final HashMap<ButtonType, Integer> buttonSlots = new HashMap<>();

        final ConfigurationSection superSection = config.getConfiguration().getConfigurationSection("lang");
        if (superSection == null) {
            Fadah.getConsole().severe("Gui Layout for the GUI %s is invalid! Missing the lang config section.".formatted(menuType.toString()));
            return;
        }
        final LanguageConfig languageConfig = new LanguageConfig(superSection);

        final ConfigurationSection layoutSection = config.getConfiguration().getConfigurationSection("layout");
        if (layoutSection == null) {
            Fadah.getConsole().severe("Gui Layout for the GUI %s is invalid! Missing the layout config section.".formatted(menuType.toString()));
            return;
        }

        final int guiSize = config.getInt("size") * 9;

        for (String key : layoutSection.getKeys(false)) {
            int slotNumber = Integer.parseInt(key);
            if (slotNumber > guiSize - 1) {
                Fadah.getConsole().severe("Gui Layout for the GUI %s is invalid! Slot: %s is out of bounds for gui size %s (%s)".formatted(menuType.toString(), slotNumber, guiSize, guiSize/9));
                return;
            }

            ButtonType buttonType;
            String temp2 = layoutSection.getString(key);
            if (temp2 == null || temp2.isBlank()) {
                Fadah.getConsole().severe("Gui Layout for the GUI %s is invalid! Slot: %s is an empty string?".formatted(menuType.toString(), slotNumber));
                return;
            }

            try {
                buttonType = ButtonType.valueOf(temp2);
            } catch (IllegalArgumentException e) {
                Fadah.getConsole().severe("Gui Layout for the GUI %s is invalid! Slot: %s Button Type %s does not exist!".formatted(menuType.toString(), slotNumber, temp2));
                return;
            }

            if (buttonType.equals(ButtonType.FILLER)) {
                fillerSlots.add(slotNumber);
                continue;
            }

            if (buttonType.equals(ButtonType.PAGINATION_ITEM)) {
                paginationSlots.add(slotNumber);
                continue;
            }

            if (buttonType.equals(ButtonType.SCROLLBAR_ITEM)) {
                scrollbarSlots.add(slotNumber);
                continue;
            }

            if (buttonType.equals(ButtonType.NO_ITEMS)) {
                paginationSlots.add(slotNumber);
                noItems.add(slotNumber);
                continue;
            }

            buttonSlots.put(buttonType, slotNumber);
        }

        guiLayouts.add(new GuiLayout(menuType, fillerSlots, paginationSlots, scrollbarSlots, noItems, buttonSlots, guiTitle, guiSize, languageConfig, config));
    }

    private MenuType getMenuType(String fileName) {
        String[] temp = fileName.split("/");
        return switch (temp[temp.length - 1]) {
            case "main.yml": yield MenuType.MAIN;
            case "new-listing.yml": yield MenuType.NEW_LISTING;
            case "profile.yml": yield MenuType.PROFILE;
            case "confirm.yml": yield MenuType.CONFIRM_PURCHASE;
            case "collection-box.yml": yield MenuType.COLLECTION_BOX;
            case "expired-listings.yml": yield MenuType.EXPIRED_LISTINGS;
            case "historic-items.yml": yield MenuType.HISTORY;
            case "view-listings.yml": yield MenuType.VIEW_LISTINGS;
            case "watch.yml": yield MenuType.WATCH;
            default: throw new IllegalStateException("The config file %s is not related to a GuiLayout".formatted(fileName));
        };
    }

    public void reloadLayout(MenuType menuType) {
        final String temp = menuType.getLayout().extraConfig().getFileName();
        guiLayouts.removeIf(mT -> mT.menuType().equals(menuType));
        loadLayout(new BasicConfig(Fadah.getInstance(), temp));
    }

    public @NotNull GuiLayout getLayout(MenuType menuType) {
        for (GuiLayout layout : guiLayouts) {
            if (menuType == layout.menuType()) return layout;
        }
        throw new IllegalStateException("No GuiLayout found for inventory type %s".formatted(menuType));
    }

    public @NotNull GuiLayout getLayout(FastInv inventory) {
        for (GuiLayout layout : guiLayouts) {
            if (inventory.getMenuType() == layout.menuType()) return layout;
        }
        throw new IllegalStateException("No GuiLayout found for inventory type %s".formatted(inventory.getMenuType()));
    }

    public enum MenuType {
        MAIN,
        NEW_LISTING,
        PROFILE,
        HISTORY,
        EXPIRED_LISTINGS,
        CONFIRM_PURCHASE,
        COLLECTION_BOX,
        VIEW_LISTINGS,
        WATCH,
        /**
         * Guis without layouts
         */
        SHULKER_PREVIEW,
        ;

        public GuiLayout getLayout() {
            return LayoutManager.instance.getLayout(this);
        }
    }

    public enum ButtonType {
        /**
         * Pagination Buttons
         */
        PAGINATION_CONTROL_ONE,
        PAGINATION_CONTROL_TWO,
        PAGINATION_ITEM,
        /**
         * Scrollbar buttons
         */
        SCROLLBAR_CONTROL_ONE,
        SCROLLBAR_CONTROL_TWO,
        SCROLLBAR_ITEM,
        /**
         * Main Menu Specific Items
         */
        PROFILE,
        FILTER,
        FILTER_DIRECTION,
        /**
         * New Listing Menu Specific Items
         */
        LISTING_START,
        LISTING_TIME,
        LISTING_ITEM,
        LISTING_MODE,
        LISTING_ADVERT,
        CURRENCY,
        /**
         * Confirm Menu Specific Items
         */
        CONFIRM,
        CANCEL,
        ITEM_TO_PURCHASE,
        /**
         * Profile Menu Specific Items
         */
        PROFILE_SUMMARY,
        PROFILE_HISTORY,
        PROFILE_COLLECTION_BOX,
        PROFILE_ACTIVE_LISTINGS,
        PROFILE_EXPIRED_LISTINGS,
        /**
         * Watch Menu Specific Items
         */
        MIN_PRICE,
        MAX_PRICE,
        /**
         * Misc Items
         */
        NO_ITEMS,
        FILLER,
        BACK,
        CLOSE,
        SEARCH,
    }
}
