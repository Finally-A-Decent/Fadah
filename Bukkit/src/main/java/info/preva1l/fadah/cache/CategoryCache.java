package info.preva1l.fadah.cache;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.processor.JavaScriptProcessor;
import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.utils.SetHelper;
import info.preva1l.fadah.utils.config.BasicConfig;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

@UtilityClass
public final class CategoryCache {
    private SortedSet<Category> categories = new TreeSet<>();
    private final BasicConfig categoriesFile = Fadah.getINSTANCE().getCategoriesFile();

    public void update() {
        categories = loadCategories();
    }

    public Category getCategory(String id) {
        return categories.stream().filter(category -> category.id().equals(id)).findFirst().orElse(null);
    }

    public String getCatName(String id) {
        if (id.equals("_none_")) {
            return "N/A";
        }
        Category category = getCategory(id);
        if (category == null) {
            return "N/A";
        }
        return category.name();
    }

    public SortedSet<Category> getCategories() {
        return Collections.unmodifiableSortedSet(categories);
    }

    public CompletableFuture<@NotNull String> getCategoryForItem(ItemStack item) {
        if (categories.isEmpty()) {
            return CompletableFuture.completedFuture("_none_");
        }
        return CompletableFuture.supplyAsync(() -> {
            for (Category category : categories) {
                for (String matcher : category.matchers()) {
                    // default to false so we don't add it to a broken category
                    if (JavaScriptProcessor.process(matcher, false, item)) {
                        return category.name();
                    }
                }
            }
            return "_none_";
        }, DatabaseManager.getInstance().getThreadPool());
    }

    public SortedSet<Category> loadCategories() {
        SortedSet<Category> set = new TreeSet<>();
        for (String key : categoriesFile.getConfiguration().getKeys(false)) {
            String name = categoriesFile.getString(key + ".name");
            Material icon = Material.getMaterial(categoriesFile.getString(key + ".icon"));
            int priority = categoriesFile.getInt(key + ".priority");
            int modelData = categoriesFile.getInt(key + ".icon-model-data");

            List<String> description = categoriesFile.getStringList(key + ".description");
            List<String> matchers = categoriesFile.getStringList(key + ".matchers");

            // legacy unused now, this is just a config fixer
            List<String> legacyMaterials = categoriesFile.getStringList(key + ".materials");
            if (!legacyMaterials.isEmpty()) {
                SetHelper.listToSet(legacyMaterials);

                // todo: e

                categoriesFile.delete(key + ".materials");
            }
            // end legacy

            set.add(
                    new Category(
                            key,
                            name,
                            priority,
                            modelData,
                            (icon == null ? Material.GRASS_BLOCK : icon),
                            description,
                            matchers
                    )
            );
        }
        return set;
    }
}