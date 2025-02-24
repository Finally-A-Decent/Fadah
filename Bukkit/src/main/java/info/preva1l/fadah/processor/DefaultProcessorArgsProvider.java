package info.preva1l.fadah.processor;

import java.util.ArrayList;

import static info.preva1l.fadah.processor.ProcessorArgType.INTEGER;
import static info.preva1l.fadah.processor.ProcessorArgType.STRING;

/**
 * Created on 20/02/2025
 *
 * @author Preva1l
 */
public interface DefaultProcessorArgsProvider {
    default void registerDefaultProcessorArgs() {
        ProcessorArgsRegistry.register(STRING, "material", item -> item.getType().toString());

        ProcessorArgsRegistry.register(STRING, "name",
                item -> item.getItemMeta().getDisplayName().replace('§', '&'));

        ProcessorArgsRegistry.register(INTEGER, "amount", item -> String.valueOf(item.getAmount()));

        ProcessorArgsRegistry.register(STRING, "lore", item -> {
            var lore = item.getItemMeta().getLore();
            if (lore == null) lore = new ArrayList<>();
            return String.join("\\n", lore);
        });
    }
}
