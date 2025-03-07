package info.preva1l.fadah.processor;

import com.google.common.base.Preconditions;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Created on 20/02/2025
 *
 * @author Preva1l
 */
public final class ProcessorArgsRegistry {
    private static final List<ProcessorArg> values = new ArrayList<>();

    public static void register(@NotNull ProcessorArgType argType, @NotNull String placeholder, Function<@NotNull ItemStack, @NotNull String> parser) {
        Preconditions.checkNotNull(parser);
        Logger.getLogger("Fadah").info("Category Matcher Provider Registered: " + placeholder);
        values.add(
                new ProcessorArg(
                        argType,
                        "%" + placeholder + "%",
                        parser
                )
        );
    }

    public static List<ProcessorArg> get() {
        return Collections.unmodifiableList(values);
    }
}