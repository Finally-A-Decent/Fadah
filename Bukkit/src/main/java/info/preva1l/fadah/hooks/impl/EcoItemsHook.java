package info.preva1l.fadah.hooks.impl;

import com.willfp.ecoitems.items.EcoItem;
import com.willfp.ecoitems.items.EcoItems;
import info.preva1l.fadah.hooks.Hook;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

@Setter
@Getter
public class EcoItemsHook implements Hook {
    private boolean enabled = false;

    public boolean isEcoItem(ItemStack item) {
        for (EcoItem ecoItem : EcoItems.INSTANCE.values()) {
            if (ecoItem.getItemStack().equals(item)) return true;
        }
        return false;
    }
}