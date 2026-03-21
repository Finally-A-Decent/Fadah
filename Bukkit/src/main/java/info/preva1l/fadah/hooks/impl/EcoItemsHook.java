package info.preva1l.fadah.hooks.impl.permissions;

import info.preva1l.fadah.filters.MatcherArgType;
import info.preva1l.fadah.filters.MatcherArgsRegistry;
import info.preva1l.hooker.annotation.Hook;
import info.preva1l.hooker.annotation.OnStart;
import info.preva1l.hooker.annotation.Require;
import org.bukkit.inventory.ItemStack;

@Hook(id = "eco-items")
@Require("EcoItems")
@Require(type = "config", value = "eco-items")
public class EcoItemsHook {

    @OnStart
    public void onStart() {
        MatcherArgsRegistry.register(MatcherArgType.STRING, "ecoitems_id", item -> getEcoItemId(item));
    }

    private String getEcoItemId(ItemStack item) {
        try {
            Class<?> itemUtils = Class.forName("com.willfp.ecoitems.items.ItemUtilsKt");
            Object ecoItem = itemUtils
                    .getMethod("getEcoItem", ItemStack.class)
                    .invoke(null, item);

            if (ecoItem == null) {
                return "";
            }

            Object id = ecoItem.getClass()
                    .getMethod("getID")
                    .invoke(ecoItem);

            return id == null ? "" : String.valueOf(id);
        } catch (ReflectiveOperationException ignored) {
            return "";
        }
    }
}
