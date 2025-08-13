package info.preva1l.fadah.config;

import com.google.common.collect.Sets;
import de.exlll.configlib.*;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.filters.MatcherService;
import info.preva1l.fadah.records.Category;
import info.preva1l.trashcan.extension.annotations.ExtensionReload;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created on 26/04/2025
 *
 * @author Preva1l
 */
@Configuration
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("FieldMayBeFinal")
public class Categories {
    private static Categories instance;
    private static Category NONE_CATEGORY = new Category(
            "_none_",
            Lang.i().getWords().getNone(),
            999999999,
            0,
            Material.AIR,
            List.of(),
            List.of()
    );

    private static final YamlConfigurationProperties PROPERTIES = YamlConfigurationProperties.newBuilder()
            .charset(StandardCharsets.UTF_8)
            .setNameFormatter(NameFormatters.LOWER_KEBAB_CASE)
            .build();

    private Set<Category> categories = Sets.newHashSet(
            new Category(
                    "tools",
                    "&aTools",
                    2,
                    0,
                    Material.DIAMOND_PICKAXE,
                    List.of(
                            "&7▪ &fPickaxes",
                            "&7▪ &fAxes",
                            "&7▪ &fShovels",
                            "&7▪ &fHoes"
                    ),
                    List.of(
                            "%material% endsWith \"_AXE\"",
                            "%material% endsWith \"_PICKAXE\"",
                            "%material% endsWith \"_HOE\"",
                            "%material% endsWith \"_SHOVEL\""
                    )
            ),
            new Category(
                    "combat",
                    "&cCombat",
                    2,
                    0,
                    Material.DIAMOND_SWORD,
                    List.of(
                            "&7▪ &fSwords",
                            "&7▪ &fArmour",
                            "&7▪ &fBows",
                            "&7▪ &fShields"
                    ),
                    List.of(
                            "%material% endsWith \"_SWORD\"",
                            "%material% endsWith \"_BOOTS\"",
                            "%material% endsWith \"_LEGGINGS\"",
                            "%material% endsWith \"_CHESTPLATE\"",
                            "%material% endsWith \"_HELMET\"",
                            "%material% == \"BOW\""
                    )
            ),
            new Category(
                    "keys",
                    "&3Crate Keys",
                    4,
                    0,
                    Material.TRIPWIRE_HOOK,
                    List.of(),
                    List.of(
                            "%material% == \"TRIPWIRE_HOOK\""
                    )
            ),
            new Category(
                    "spawners-and-eggs",
                    "&5Spawners and Mob Eggs",
                    2,
                    0,
                    Material.CHICKEN_SPAWN_EGG,
                    List.of(
                            "&fAll Mob Spawners and Mob Spawn Eggs"
                    ),
                    List.of(
                            "%material% == \"SPAWNER\"",
                            "%material% endsWith \"_SPAWN_EGG\""
                    )
            ),
            new Category(
                    "building-blocks",
                    "&dBuilding Blocks",
                    2,
                    0,
                    Material.BRICKS,
                    List.of(
                            "&fBlocks used to make &ebeautiful &fbuildings!"
                    ),
                    List.of(
                            "%material% == \"COARSE_DIRT\"",
                            "%material% == \"TERRACOTTA\"",
                            "%material% endsWith \"_WOOL\"",
                            "%material% endsWith \"_TERRACOTTA\"",
                            "%material% endsWith \"_CONCRETE\"",
                            "%material% endsWith \"_CONCRETE_POWDER\"",
                            "%material% == \"GLASS\"",
                            "%material% endsWith \"_GLASS\"",
                            "%material% endsWith \"_GLASS_PANE\""
                    )
            ),
            new Category(
                    "redstone",
                    "&cRedstone",
                    3,
                    0,
                    Material.REPEATER,
                    List.of(
                            "&fRedstone components."
                    ),
                    List.of(
                            "%material% contains \"REDSTONE\"",
                            "%material% == \"REPEATER\"",
                            "%material% == \"COMPARATOR\""
                    )
            ),
            new Category(
                    "eco-items",
                    "&fEco Items Example",
                    3,
                    0,
                    Material.BLUE_DYE,
                    List.of(
                            "&fAn example usage for the ecoitems hook"
                    ),
                    List.of(
                            "%ecoitems_id% == \"your_custom_item\""
                    )
            ),
            new Category(
                    "misc",
                    "&eMisc",
                    1,
                    0,
                    Material.LEATHER,
                    List.of(
                            "&fItems that don't fit under any other category."
                    ),
                    List.of(
                            "true"
                    )
            )
    );

    @Ignore
    private final SortedSet<Category> sortedCache = new TreeSet<>();
    @Ignore
    private final SortedSet<Category> customViaApi = new TreeSet<>();

    public static SortedSet<Category> getCategories() {
        SortedSet<Category> categories = i().sortedCache;
        categories.removeIf(it -> it.id().equals("_none_"));
        return categories;
    }

    public static Optional<Category> getCategory(String id) {
        return i().sortedCache.stream().filter(category -> category.id().equals(id)).findFirst();
    }

    public static String getCategoryForItem(ItemStack item) {
        if (i().sortedCache.isEmpty()) return "_none_";

        for (Category category : i().sortedCache) {
            for (String matcher : category.matchers()) {
                // default to false so we don't add it to a broken category
                if (MatcherService.instance.process(matcher, false, item)) return category.id();
            }
        }
        return "_none_";
    }

    public static boolean registerCategory(@NotNull Category category) {
        i().customViaApi.add(category);
        return i().sortedCache.add(category);
    }

    public static boolean unregisterCategory(@NotNull String id) {
        i().customViaApi.removeIf(category -> category.id().equals(id));
        return i().sortedCache.removeIf(category -> category.id().equals(id));
    }

    @ExtensionReload
    public static void reload() {
        instance = YamlConfigurations.load(new File(Fadah.getInstance().getDataFolder(), "categories.yml").toPath(), Categories.class, PROPERTIES);
        instance.categories.add(NONE_CATEGORY);
        instance.sortedCache.clear();
        instance.sortedCache.addAll(instance.categories);
        instance.sortedCache.addAll(instance.customViaApi);
    }

    public static Categories i() {
        if (instance != null) {
            return instance;
        }

        instance = YamlConfigurations.update(new File(Fadah.getInstance().getDataFolder(), "categories.yml").toPath(), Categories.class, PROPERTIES);
        instance.categories.add(NONE_CATEGORY);
        instance.sortedCache.clear();
        instance.sortedCache.addAll(instance.categories);
        instance.sortedCache.addAll(instance.customViaApi);
        return instance;
    }
}