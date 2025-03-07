package info.preva1l.fadah.utils;

import info.preva1l.fadah.config.Config;
import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String Formatting Helper.
 */
@UtilityClass
public class StringUtils {
    private final LegacyComponentSerializer legacyComponentSerializer = LegacyComponentSerializer.legacyAmpersand();
    private final Pattern HEX_PATTERN = Pattern.compile("&#(\\w{5}[0-9a-fA-F])");
    private final Pattern MM_HEX_PATTERN = Pattern.compile("<#[a-fA-F0-9]{6}>");
    private final Pattern SECTION_SYMBOL_PATTERN = Pattern.compile("§[0-9a-fA-Fk-orK-OR]");
    private final Pattern AMPERSAND_PATTERN = Pattern.compile("&[0-9a-fA-Fk-orK-OR]");

    /**
     * Colorize a list. (Useful for lore)
     *
     * @param list List typeof String
     * @return Colorized List typeof String
     */
    public List<String> colorizeList(List<String> list) {
        return colorizeList(null, list);
    }

    public List<String> colorizeList(@Nullable Player player, List<String> list) {
        if (list == null) return null;
        if (list.isEmpty()) return null;
        List<String> ret = new ArrayList<>();
        for (String line : list) ret.add(colorize(player, line));
        return ret;
    }

    /**
     * Converts MiniMessage to legacy colour codes.
     * @param message message with mini message formatting
     * @return string with legacy formatting (not colorized)
     */
    public String miniMessageToLegacy(String message) {
        message = message.replace("<dark_red>", "&4");
        message = message.replace("<red>", "&c");
        message = message.replace("<gold>", "&6");
        message = message.replace("<yellow>", "&e");
        message = message.replace("<dark_green>", "&2");
        message = message.replace("<green>", "&a");
        message = message.replace("<aqua>", "&b");
        message = message.replace("<dark_aqua>", "&3");
        message = message.replace("<dark_blue>", "&1");
        message = message.replace("<blue>", "&9");
        message = message.replace("<light_purple>", "&d");
        message = message.replace("<dark_purple>", "&5");
        message = message.replace("<white>", "&f");
        message = message.replace("<gray>", "&7");
        message = message.replace("<dark_gray>", "&8");
        message = message.replace("<black>", "&0");
        message = message.replace("<b>", "&l");
        message = message.replace("<bold>", "&l");
        message = message.replace("<obf>", "&k");
        message = message.replace("<obfuscated>", "&k");
        message = message.replace("<st>", "&m");
        message = message.replace("<strikethrough>", "&m");
        message = message.replace("<u>", "&n");
        message = message.replace("<underline>", "&n");
        message = message.replace("<i>", "&o");
        message = message.replace("<italic>", "&o");
        message = message.replace("<reset>", "&r");
        message = message.replace("<r>", "&r");

        Matcher match = MM_HEX_PATTERN.matcher(message);
        StringBuilder result = new StringBuilder();

        while (match.find()) {
            String code = match.group();
            String replacement = code.replace("<", "&").replace(">", "");
            match.appendReplacement(result, replacement);
        }
        match.appendTail(result);

        return result.toString();
    }

    /**
     * Converts legacy colour codes to MiniMessage.
     * @param message message with legacy codes
     * @return string with mini modernMessage formatting (not colorized)
     */
    public String legacyToMiniMessage(String message) {
        return legacyComponentSerializer.serialize(legacyComponentSerializer.deserialize(message.replace('§', '&')));
    }

    /**
     * Colorize  a string.
     * @param text String with color codes or hex codes.
     * @return Colorized String
     */
    public String colorize(String text) {
        return colorize(null, text);
    }

    public String colorize(@Nullable Player player, String text) {
        text = miniMessageToLegacy(text);
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder buffer = new StringBuilder();

        while(matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
        }

        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    /**
     * Strip color codes from a string, including hex codes, codes starting with the section symbol (§),
     * and codes starting with an ampersand (&).
     *
     * @param str String with color codes.
     * @return String without color codes.
     */
    public String removeColorCodes(String str) {
        // Remove hex codes
        String result = HEX_PATTERN.matcher(str).replaceAll("");
        // Remove section symbol codes
        result = SECTION_SYMBOL_PATTERN.matcher(result).replaceAll("");
        // Remove ampersand codes
        result = AMPERSAND_PATTERN.matcher(result).replaceAll("");
        return result;
    }

    public double getAmountFromString(String priceString) throws NumberFormatException {
        if (priceString.toLowerCase().contains("nan")) {
            throw new NumberFormatException();
        }

        double multi = 1;

        if (priceString.toLowerCase().endsWith("k")) {
            multi = 1000;
            priceString = priceString.replace("k", "");
            priceString = priceString.replace("K", "");
        } else if (priceString.toLowerCase().endsWith("m")) {
            multi = 1_000_000;
            priceString = priceString.replace("m", "");
            priceString = priceString.replace("M", "");
        } else if (priceString.toLowerCase().endsWith("b")) {
            multi = 1_000_000_000;
            priceString = priceString.replace("b", "");
            priceString = priceString.replace("B", "");
        } else if (priceString.toLowerCase().endsWith("t")) {
            multi = 1_000_000_000_000L;
            priceString = priceString.replace("t", "");
            priceString = priceString.replace("T", "");
        } else if (priceString.toLowerCase().endsWith("q")) {
            multi = 1_000_000_000_000_000L;
            priceString = priceString.replace("q", "");
            priceString = priceString.replace("Q", "");
        }

        return Double.parseDouble(priceString) * multi;
    }

    /**
     * Formats a string into a component.
     *
     * @param message string with mini message formatted colours and or placeholders
     * @param args    arguments for {@link StringUtils#formatPlaceholders(String, Object...)}
     * @return formatted component
     */
    public String message(String message, Object... args) {
        message = formatPlaceholders(message, args);

        return colorize(message);
    }

    /**
     * Formats Strings with placeholders
     *
     * @param message message with placeholders: {index}
     * @param args    things to replace with
     * @return formatted string
     */
    public String formatPlaceholders(String message, Object... args) {
        for (int i = 0; i < args.length; i++) {
            if (!message.contains("{" + i + "}")) {
                continue;
            }

            message = message.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return message;
    }

    /**
     * Capitalizes the first letter in a string.
     * @param str String
     * @return Capitalized String
     */
    public String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Gets an item name from an item stack
     * @param item item stack
     * @return formatted item name
     */
    public String extractItemName(ItemStack item) {
        if (item.getItemMeta() != null && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        if (item.getItemMeta() != null && item.getItemMeta().hasLocalizedName()) {
            return item.getItemMeta().getLocalizedName();
        }
        String[] split = item.getType().name().toLowerCase().split("_");
        StringBuilder builder = new StringBuilder();
        for (String s : split) {
            builder.append(capitalize(s)).append(" ");
        }
        return builder.toString().trim().replace("§", "&");
    }

    /**
     * @return true if the item contains the search
     */
    public boolean doesItemHaveString(String toCheck, ItemStack item) {
        if (Config.i().getSearch().isType()) {
            if (item.getType().name().toUpperCase().contains(toCheck.toUpperCase())
                    || item.getType().name().toUpperCase().contains(toCheck.replace(" ", "_").toUpperCase())) {
                return true;
            }
        }

        if (item.getItemMeta() != null) {
            if (Config.i().getSearch().isName()) {
                if (item.getItemMeta().getDisplayName().toUpperCase().contains(toCheck.toUpperCase())) {
                    return true;
                }
            }

            if (Config.i().getSearch().isLore()) {
                return item.getItemMeta().getLore() != null && item.getItemMeta().getLore().contains(toCheck.toUpperCase());
            }
        }
        return false;
    }
}
