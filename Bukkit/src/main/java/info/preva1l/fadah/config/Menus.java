package info.preva1l.fadah.config;

import de.exlll.configlib.Configuration;
import de.exlll.configlib.NameFormatters;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.misc.ConfigurableItem;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Material;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Created on 20/03/2025
 *
 * @author Preva1l
 */
@Getter
@Configuration
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("FieldMayBeFinal")
public class Menus {
    private static Menus instance;

    private static final YamlConfigurationProperties PROPERTIES = YamlConfigurationProperties.newBuilder()
            .charset(StandardCharsets.UTF_8)
            .setNameFormatter(NameFormatters.LOWER_KEBAB_CASE).build();

    private String searchTitle = "&9&lAuction House &8> &fSearch";

    private ConfigurableItem noItemFound = new ConfigurableItem(
            Material.BARRIER,
            0,
            "&c&lNo items found!",
            List.of()
    );

    private ConfigurableItem backButton = new ConfigurableItem(
            Material.FEATHER,
            0,
            "&cGo Back",
            List.of()
    );

    private ConfigurableItem previousButton = new ConfigurableItem(
            Material.ARROW,
            0,
            "&c&lPrevious Page",
            List.of()
    );

    private ConfigurableItem nextButton = new ConfigurableItem(
            Material.ARROW,
            0,
            "&a&lNext Page",
            List.of()
    );

    private ConfigurableItem scrollNextButton = new ConfigurableItem(
            Material.ARROW,
            0,
            "&a&lScroll Categories Down",
            List.of()
    );

    private ConfigurableItem scrollPreviousButton = new ConfigurableItem(
            Material.ARROW,
            0,
            "&a&lScroll Categories Up",
            List.of()
    );

    private ConfigurableItem closeButton = new ConfigurableItem(
            Material.BARRIER,
            0,
            "&c&l✗ Close",
            List.of()
    );

    private ConfigurableItem border = new ConfigurableItem(
            Material.BLACK_STAINED_GLASS_PANE,
            0,
            "&r ",
            List.of()
    );

    public static void reload() {
        instance = YamlConfigurations.load(new File(Fadah.getInstance().getDataFolder(), "menus/misc.yml").toPath(), Menus.class, PROPERTIES);
    }

    public static Menus i() {
        if (instance != null) {
            return instance;
        }

        return instance = YamlConfigurations.update(new File(Fadah.getInstance().getDataFolder(), "menus/misc.yml").toPath(), Menus.class, PROPERTIES);
    }
}
