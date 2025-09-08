package info.preva1l.fadah.config.upgraders.impl;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.upgraders.ConfigUpgrader;
import lombok.AllArgsConstructor;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created on 16/06/2025
 *
 * @author Preva1l
 */
@AllArgsConstructor
public class CurrencyConfigUpgrader implements ConfigUpgrader {
    private final Logger logger;

    @Override
    public void migrate() {
        File folder = Fadah.instance.getDataFolder();
        File oldFile = new File(folder, "config.yml");
        File newFile = new File(folder, "currencies.yml");

        if (!oldFile.exists()) return;
        if (newFile.exists()) return;

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        try (InputStream inputStream = new FileInputStream(oldFile)) {
            Map<String, Object> config = yaml.load(inputStream);
            Map<String, Object> currency = (Map<String, Object>) config.get("currency");
            if (currency == null || currency.isEmpty()) return;

            Map<String, Object> vault = (Map<String, Object>) currency.get("vault");
            vault.put("enabled", true);

            Map<String, Object> playerpoints = (Map<String, Object>) currency.get("player-points");
            playerpoints.put("enabled", true);

            parse(currency.get("coins-engine"));
            parse(currency.get("redis-economy"));

            try (Writer writer = new FileWriter(newFile)) {
                yaml.dump(currency, writer);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to migrate currencies config!", e);
        }
    }

    private void parse(Object obj) {
        Map<String, Object> eco = (Map<String, Object>) obj;
        eco.put("enabled", true);
        List<Map<String, Object>> subs = (List<Map<String, Object>>) eco.get("currencies");
        for (Map<String, Object> sub : subs) {
            sub.put("symbol", '$');
        }
    }
}