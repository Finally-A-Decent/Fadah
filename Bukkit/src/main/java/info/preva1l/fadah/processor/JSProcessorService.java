package info.preva1l.fadah.processor;

import info.preva1l.trashcan.flavor.annotations.Configure;
import info.preva1l.trashcan.flavor.annotations.Service;
import info.preva1l.trashcan.flavor.annotations.inject.Inject;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Nullable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Scriptable;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static info.preva1l.fadah.processor.ProcessorArgType.INTEGER;
import static info.preva1l.fadah.processor.ProcessorArgType.STRING;

/**
 * Created on 20/02/2025
 *
 * @author Preva1l
 */
@Service
public final class JSProcessorService {
    public static final JSProcessorService instance = new JSProcessorService();

    @Inject private Logger logger;

    @Configure
    public void configure() {
        ProcessorArgsRegistry.register(STRING, "material", item -> item.getType().toString());

        ProcessorArgsRegistry.register(STRING, "name", item -> ((TextComponent) item.ef).content());

        ProcessorArgsRegistry.register(INTEGER, "amount", item -> String.valueOf(item.getAmount()));

        ProcessorArgsRegistry.register(STRING, "lore", item -> {
            var lore = item.getItemMeta().getLore();
            if (lore == null) lore = new ArrayList<>();
            return String.join("\\n", lore);
        });
    }

    @Blocking
    public Boolean process(String expression, boolean def) {
        return process(expression, def, null);
    }

    /**
     * Process a javascript expression that results in a boolean
     */
    @Blocking
    public Boolean process(String expression, boolean def, @Nullable ItemStack item) {
        if (item != null) {
            for (ProcessorArg replacement : ProcessorArgsRegistry.get()) {
                if (replacement.type() == ProcessorArgType.STRING) {
                    expression = expression.replace(replacement.placeholder(), "\"" + escape(replacement.parse(item)) + "\"");
                } else {
                    expression = expression.replace(replacement.placeholder(), escape(replacement.parse(item)));
                }
            }
        }

        boolean result;
        try (Context cx = Context.enter()) {
            Scriptable scope = cx.initSafeStandardObjects();
            result = (Boolean) cx.evaluateString(scope, expression, "Fadah", 1, null);
            return result;
        } catch (EvaluatorException | ClassCastException e) {
            if (!e.getMessage().contains("syntax error")) {
                logger.log(Level.SEVERE,
                        """
                        Unable to process expression: '%s'
                        (Report this to Fadah support)
                        """.stripIndent().formatted(expression),
                        e);
                return def;
            }
            logger.severe(
                    """
                    Unable to process expression: '%s'
                    This is likely related to a category matcher or a item blacklist.
                    DO NOT REPORT THIS TO Fadah SUPPORT, THIS IS NOT A BUG, THIS IS A CONFIGURATION PROBLEM.
                    """.stripIndent().formatted(expression)
            );
            return def;
        } catch (Exception e) {
            logger.log(Level.SEVERE,
                    """
                    Unable to process expression: '%s'
                    (Report this to Fadah support)
                    """.stripIndent().formatted(expression),
                    e);
            return def;
        }
    }

    private String escape(String input) {
        input = input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("`", "\\`")
                .replaceAll("%[^%]+%", "\"UNKNOWN_VALUE\"");
        return input;
    }
}
