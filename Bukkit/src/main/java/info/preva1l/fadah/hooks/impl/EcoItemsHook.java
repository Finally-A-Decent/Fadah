package info.preva1l.fadah.hooks.impl;

import com.willfp.ecoitems.items.ItemUtilsKt;
import info.preva1l.fadah.hooks.Hook;
import info.preva1l.fadah.processor.ProcessorArgType;
import info.preva1l.fadah.processor.ProcessorArgsRegistry;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EcoItemsHook implements Hook {
    private boolean enabled = false;

    public EcoItemsHook() {
        ProcessorArgsRegistry.register(ProcessorArgType.STRING, "ecoitems_id", item -> {
            var ecoitem = ItemUtilsKt.getEcoItem(item);
            if (ecoitem == null) return "";
            return ecoitem.getID();
        });
    }
}