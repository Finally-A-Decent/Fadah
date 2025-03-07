package info.preva1l.fadah.hooks.impl;

import com.willfp.ecoitems.items.ItemUtilsKt;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.hooks.Hook;
import info.preva1l.fadah.processor.ProcessorArgType;
import info.preva1l.fadah.processor.ProcessorArgsRegistry;

public class EcoItemsHook extends Hook {
    @Override
    protected boolean onEnable() {
        if (!Config.i().getHooks().isEcoItems()) return false;

        ProcessorArgsRegistry.register(ProcessorArgType.STRING, "ecoitems_id", item -> {
            var ecoitem = ItemUtilsKt.getEcoItem(item);
            if (ecoitem == null) return "";
            return ecoitem.getID();
        });

        return true;
    }
}