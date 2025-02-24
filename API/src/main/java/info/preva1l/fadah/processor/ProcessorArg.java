package info.preva1l.fadah.processor;

import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

/**
 * Created on 20/02/2025
 *
 * @author Preva1l
 */
public record ProcessorArg(
        ProcessorArgType type,
        String placeholder,
        Function<ItemStack, String> parser
) {
    public String parse(ItemStack item) {
        return parser.apply(item);
    }
}
