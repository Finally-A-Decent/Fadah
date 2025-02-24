package info.preva1l.fadah.utils.config;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class BasicConfig {
    private final JavaPlugin plugin;
    private final String fileName;
    private YamlConfiguration configuration;

    private File file;

    public BasicConfig(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        this.file = new File(plugin.getDataFolder(), fileName);
        if (!this.file.exists()) {
            plugin.saveResource(fileName, false);
        }
        this.configuration = YamlConfiguration.loadConfiguration(this.file);
    }

    public boolean getBoolean(String path) {
        return (this.configuration.contains(path)) && (this.configuration.getBoolean(path));
    }

    public int getInt(String path) {
        if (this.configuration.contains(path)) {
            return this.configuration.getInt(path);
        }
        return 0;
    }

    public String getString(String path) {
        if (this.configuration.contains(path)) {
            return this.configuration.getString(path);
        }
        return path;
    }

    public List<String> getStringList(String path) {
        if (this.configuration.contains(path)) {
            return this.configuration.getStringList(path);
        }
        return Collections.emptyList();
    }

    public void delete(String path) {
        if (this.configuration.contains(path)) {
            this.configuration.set(path, null);
            save();
        }
    }

    public void load() {
        this.file = new File(plugin.getDataFolder(), fileName);
        if (!this.file.exists()) {
            plugin.saveResource(fileName, false);
        }
        this.configuration = YamlConfiguration.loadConfiguration(this.file);
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public void save() {
        try {
            this.configuration.save(this.file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}