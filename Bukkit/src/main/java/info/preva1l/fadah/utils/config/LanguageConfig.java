package info.preva1l.fadah.utils.config;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.utils.StringUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LanguageConfig {
    private final ConfigurationSection superSection;

    public LanguageConfig(@NotNull ConfigurationSection superSection) {
        this.superSection = superSection;
    }

    public int getInt(String path, int def) {
        return superSection.getInt(path, def);
    }

    public int getInt(String path) {
        return superSection.getInt(path, 0);
    }

    public @NotNull Material getAsMaterial(String path) {
        Material material;
        String s = superSection.getString(path);
        if (s == null || s.isEmpty()) {
            throw new RuntimeException("No value at path %s".formatted(path));
        }
        try {
            material = Material.valueOf(s.toUpperCase());
        } catch (EnumConstantNotPresentException | IllegalArgumentException e) {
            material = Material.APPLE;
            Fadah.getConsole().severe("-----------------------------");
            Fadah.getConsole().severe("Config Incorrect!");
            Fadah.getConsole().severe("Material: " + s);
            Fadah.getConsole().severe("Does Not Exist!");
            Fadah.getConsole().severe("Defaulting to APPLE");
            Fadah.getConsole().severe("-----------------------------");
        }
        return material;
    }

    public @NotNull Material getAsMaterial(String path, Material def) {
        Material material;
        String s = superSection.getString(path);
        if (s == null || s.isEmpty()) {
            throw new RuntimeException("No value at path %s".formatted(path));
        }
        try {
            material = Material.valueOf(s.toUpperCase());
        } catch (EnumConstantNotPresentException | IllegalArgumentException e) {
            material = def;
            Fadah.getConsole().severe("-----------------------------");
            Fadah.getConsole().severe("Config Incorrect!");
            Fadah.getConsole().severe("Material: " + s);
            Fadah.getConsole().severe("Does Not Exist!");
            Fadah.getConsole().severe("Defaulting to " + def.toString());
            Fadah.getConsole().severe("-----------------------------");
        }
        return material;
    }

    public @NotNull String getStringFormatted(String path)  {
        String f = superSection.getString(path);
        if (f == null || f.equals(path)) {
            throw new RuntimeException("No value at path %s".formatted(path));
        }
        return StringUtils.colorize(f);
    }

    public @NotNull String getString(String path, String def) {
        return superSection.getString(path, def);
    }

    public @NotNull String getStringFormatted(String path, String def) {
        return StringUtils.colorize(superSection.getString(path, def));
    }

    public @NotNull String getStringFormatted(String path, String def, Object... replacements) {
        String f = superSection.getString(path);
        if (f == null || f.equals(path)) {
            return def;
        }
        return StringUtils.colorize(StringUtils.formatPlaceholders(f, replacements));
    }

    public @NotNull List<String> getLore(String path) {
        List<String> str = superSection.getStringList(path);
        if (str.isEmpty() || str.get(0).equals(path) || str.get(0).equals("null")) {
            return Collections.emptyList();
        }
        return StringUtils.colorizeList(str);
    }

    public @NotNull List<String> getLore(String path, List<String> def) {
        List<String> str = superSection.getStringList(path);
        if (str.isEmpty() || str.get(0).equals(path) || str.get(0).equals("null")) {
            return StringUtils.colorizeList(def);
        }
        return StringUtils.colorizeList(str);
    }

    public @NotNull List<String> getLore(String path, Object... replacements) {
        return getLore(null, path, replacements);
    }

    public @NotNull List<String> getLore(Player player, String path, Object... replacements) {
        List<String> str = superSection.getStringList(path);
        if (str.isEmpty() || str.get(0).equals(path) || str.get(0).equals("null")) {
            return Collections.emptyList();
        }
        List<String> ret = new ArrayList<>();
        for (String line : str) {
            ret.add(StringUtils.formatPlaceholders(line, replacements));
        }
        return StringUtils.colorizeList(player, ret);
    }

    public @NotNull List<String> getLore(String path, List<String> def, Object... replacements) {
        List<String> str = superSection.getStringList(path);
        if (str.isEmpty() || str.get(0).equals(path) || str.get(0).equals("null")) {
            List<String> ret = new ArrayList<>();
            for (String line : def) {
                ret.add(StringUtils.formatPlaceholders(line, replacements));
            }
            return StringUtils.colorizeList(ret);
        }
        List<String> ret = new ArrayList<>();
        for (String line : str) {
            ret.add(StringUtils.formatPlaceholders(line, replacements));
        }
        return StringUtils.colorizeList(ret);
    }
}
