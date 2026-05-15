package info.preva1l.fadah.hooks.impl;

import com.willfp.ecoitems.items.ItemUtilsKt;
import info.preva1l.fadah.filters.MatcherArgType;
import info.preva1l.fadah.filters.MatcherArgsRegistry;

public class EcoItemsHook {
    public void onStart() {
        MatcherArgsRegistry.register(MatcherArgType.STRING, "ecoitems_id", item -> {
            var ecoitem = ItemUtilsKt.getEcoItem(item);
            if (ecoitem == null) return "";
            return ecoitem.getID();
        });
    }
}
