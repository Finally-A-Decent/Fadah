package info.preva1l.fadah.config;


import com.google.common.collect.ImmutableList;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.config.BasicConfig;
import lombok.AllArgsConstructor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
@SuppressWarnings("unchecked")
public enum Menus {
    SEARCH_TITLE("search-title", "&9&lAuction House &8> &fSearch"),

    NO_ITEM_FOUND_ICON("no-items-found.icon", "BARRIER"),
    NO_ITEM_FOUND_MODEL_DATA("no-items-found.model-data", 0),
    NO_ITEM_FOUND_NAME("no-items-found.name", "&c&lNo items found!"),
    NO_ITEM_FOUND_LORE("no-items-found.lore", Collections.emptyList()),

    BACK_BUTTON_ICON("back.icon", "FEATHER"),
    BACK_BUTTON_MODEL_DATA("back.model-data", 0),
    BACK_BUTTON_NAME("back.name", "&cGo Back"),
    BACK_BUTTON_LORE("back.lore", Collections.emptyList()),

    PREVIOUS_BUTTON_ICON("previous-page.icon", "ARROW"),
    PREVIOUS_BUTTON_MODEL_DATA("previous-page.model-data", 0),
    PREVIOUS_BUTTON_NAME("previous-page.name", "&c&lPrevious Page"),
    PREVIOUS_BUTTON_LORE("previous-page.lore", Collections.emptyList()),

    NEXT_BUTTON_ICON("next-page.icon", "ARROW"),
    NEXT_BUTTON_MODEL_DATA("next-page.model-data", 0),
    NEXT_BUTTON_NAME("next-page.name", "&a&lNext Page"),
    NEXT_BUTTON_LORE("next-page.lore", Collections.emptyList()),

    SCROLL_NEXT_BUTTON_ICON("scroll-next.icon", "ARROW"),
    SCROLL_NEXT_BUTTON_MODEL_DATA("scroll-next.model-data", 0),
    SCROLL_NEXT_BUTTON_NAME("scroll-next.name", "&a&lScroll Categories Down"),
    SCROLL_NEXT_BUTTON_LORE("scroll-next.lore", Collections.emptyList()),

    SCROLL_PREVIOUS_BUTTON_ICON("scroll-previous.icon", "ARROW"),
    SCROLL_PREVIOUS_BUTTON_MODEL_DATA("scroll-previous.model-data", 0),
    SCROLL_PREVIOUS_BUTTON_NAME("scroll-previous.name", "&a&lScroll Categories Up"),
    SCROLL_PREVIOUS_BUTTON_LORE("scroll-previous.lore", Collections.emptyList()),
   
    CLOSE_BUTTON_ICON("close.icon", "BARRIER"),
    CLOSE_BUTTON_MODEL_DATA("close.model-data", 0),
    CLOSE_BUTTON_NAME("close.name", "&c&l✗ Close"),
    CLOSE_BUTTON_LORE("close.lore", Collections.emptyList()),
    
    BORDER_ICON("filler.icon", "BLACK_STAINED_GLASS_PANE"),
    BORDER_MODEL_DATA("filler.model-data", 0),
    BORDER_NAME("filler.name", "&r "),
    BORDER_LORE("filler.lore", Collections.emptyList()),
    ;

    private final String path;
    private final Object defaultValue;

    public static void loadDefault() {
        BasicConfig configFile = Fadah.getINSTANCE().getMenusFile();

        for (Menus config : Menus.values()) {
            String path = config.path;
            String str = configFile.getString(path);
            if (str.equals(path)) {
                configFile.getConfiguration().set(path, config.defaultValue);
            }
        }

        configFile.save();
        configFile.load();
    }

    @Override
    public String toString() {
        String str = Fadah.getINSTANCE().getMenusFile().getString(path);
        if (str.equals(path)) {
            return defaultValue.toString();
        }
        return str;
    }

    public String toFormattedString() {
        String str = Fadah.getINSTANCE().getMenusFile().getString(path);
        if (str.equals(path)) {
            return StringUtils.colorize(defaultValue.toString());
        }
        return StringUtils.colorize(str);
    }

    public String toFormattedString(Object... replacements) {
        String str = Fadah.getINSTANCE().getMenusFile().getString(path);
        if (str.equals(path)) {
            return StringUtils.formatPlaceholders(StringUtils.colorize(defaultValue.toString()), replacements);
        }
        return StringUtils.colorize(StringUtils.formatPlaceholders(str, replacements));
    }

    public List<String> toLore() {
        List<String> str = Fadah.getINSTANCE().getMenusFile().getStringList(path);
        if (str.isEmpty()) {
            List<String> ret = new ArrayList<>();
            for (String line : (List<String>) defaultValue) ret.add(StringUtils.formatPlaceholders(line));
            return StringUtils.colorizeList(ret);
        }
        if (str.get(0).equals("null")) {
            return null;
        }
        List<String> ret = new ArrayList<>();
        for (String line : str) ret.add(StringUtils.formatPlaceholders(line));
        return StringUtils.colorizeList(ret);
    }

    public List<String> toLore(Object... replacements) {
        List<String> str = Fadah.getINSTANCE().getMenusFile().getStringList(path);
        if (str.isEmpty()) {
            List<String> ret = new ArrayList<>();
            for (String line : (List<String>) defaultValue) ret.add(StringUtils.formatPlaceholders(line, replacements));
            return StringUtils.colorizeList(ret);
        }
        if (str.get(0).equals("null")) {
            return ImmutableList.of();
        }
        List<String> ret = new ArrayList<>();
        for (String line : str) {
            ret.add(StringUtils.formatPlaceholders(line, replacements));
        }
        return StringUtils.colorizeList(ret);
    }

    public int toInteger() {
        return Integer.parseInt(toString());
    }

    public Material toMaterial() {
        Material material;
        try {
            material = Material.valueOf(toString().toUpperCase());
        } catch (EnumConstantNotPresentException | IllegalArgumentException e) {
            material = Material.APPLE;
            Fadah.getConsole().severe("-----------------------------");
            Fadah.getConsole().severe("Config Incorrect!");
            Fadah.getConsole().severe("Material: " + toFormattedString());
            Fadah.getConsole().severe("Does Not Exist!");
            Fadah.getConsole().severe("Defaulting to APPLE");
            Fadah.getConsole().severe("-----------------------------");
        }
        return material;
    }
}
