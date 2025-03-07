package info.preva1l.fadah.cache;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.processor.JavaScriptProcessor;
import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.utils.config.BasicConfig;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@UtilityClass
public final class CategoryRegistry {
    private final SortedSet<Category> categories = new TreeSet<>();
    private final BasicConfig categoriesFile = Fadah.getINSTANCE().getCategoriesFile();

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
                        return category.id();
                    }
                }
            }
            return "_none_";
        }, DatabaseManager.getInstance().getThreadPool());
    }

    public void loadCategories() {
        Fadah.getINSTANCE().getCategoriesFile().load();
        SortedSet<Category> set = new TreeSet<>();
        for (String key : categoriesFile.getConfiguration().getKeys(false)) {
            String name = categoriesFile.getString(key + ".name");
            Material icon = Material.getMaterial(categoriesFile.getString(key + ".icon"));
            int priority = categoriesFile.getInt(key + ".priority");
            int modelData = categoriesFile.getInt(key + ".icon-model-data");

            List<String> description = categoriesFile.getStringList(key + ".description");
            List<String> matchers = new ArrayList<>(categoriesFile.getStringList(key + ".matchers"));

            // legacy unused now, this is just a config fixer
            List<String> legacyMaterials = categoriesFile.getStringList(key + ".materials");
            if (!legacyMaterials.isEmpty()) {
                matchers.addAll(legacyMaterialsListToMatcherList(legacyMaterials));
                categoriesFile.delete(key + ".materials");
                categoriesFile.setStringList(key + ".matchers", matchers);
                categoriesFile.save();
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
        categories.removeAll(set);
        categories.addAll(set);
    }

    public boolean registerCategory(@NotNull Category category) {
        return categories.add(category);
    }

    public boolean unregisterCategory(@NotNull String id) {
        return categories.removeIf(category -> category.id().equals(id));
    }

    private List<String> legacyMaterialsListToMatcherList(List<String> strings) {
        return strings.stream()
                .map(s -> {
                    if (s.startsWith("*_")) return "%material%.endsWith(\"" + s.replace("*", "") + "\")";
                    else if (s.endsWith("_*")) return "%material%.startsWith(\"" + s.replace("*", "") + "\")";
                    else if (s.equals("*")) return "true";
                    else return "%material% == \"" + s + "\"";
                })
                .collect(Collectors.toList());
    }
}