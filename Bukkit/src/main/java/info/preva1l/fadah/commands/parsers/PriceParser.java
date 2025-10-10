package info.preva1l.fadah.commands.parsers;

import info.preva1l.fadah.utils.Text;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.parser.standard.NumberParser;
import org.incendo.cloud.type.range.DoubleRange;
import org.incendo.cloud.type.range.Range;

/**
 * Created on 19/05/2025
 *
 * @author Preva1l
 */
public final class PriceParser<C> extends NumberParser<C, Double, DoubleRange> {
    public static <C> ParserDescriptor<C, Double> create() {
        return ParserDescriptor.of(new PriceParser<>(), Double.class);
    }

    private PriceParser() {
        super(Range.doubleRange(0.0, Double.MAX_VALUE));
    }

    @Override
    public boolean hasMax() {
        return false;
    }

    @Override
    public boolean hasMin() {
        return true;
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull Double> parse(@NonNull CommandContext<@NonNull C> commandContext, @NonNull CommandInput commandInput) {
        String input = commandInput.readString();
        if (input.toLowerCase().contains("nan") || input.toLowerCase().contains("infinity")) return ArgumentParseResult.failure(new RuntimeException("NaN or infinity"));

        try {
            return ArgumentParseResult.success(Text.getAmountFromString(input));
        } catch (NumberFormatException e) {
            return ArgumentParseResult.failure(e);
        }
    }
}
