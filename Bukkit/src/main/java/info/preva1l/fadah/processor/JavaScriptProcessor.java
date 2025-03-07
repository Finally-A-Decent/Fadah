package info.preva1l.fadah.processor;

import info.preva1l.fadah.Fadah;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Nullable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.util.logging.Level;

/**
 * Created on 20/02/2025
 *
 * @author Preva1l
 */
public class JavaScriptProcessor {
    @Blocking
    public static Boolean process(String expression, boolean def) {
        return process(expression, def, null);
    }

    /**
     * Process a javascript expression that results in a boolean
     */
    @Blocking
    public static Boolean process(String expression, boolean def, @Nullable ItemStack item) {
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
        } catch (ClassCastException e) {
            Fadah.getConsole().severe(
                    """
                    Unable to process expression: '%s'
                    This is likely related to a category matcher or a item blacklist.
                    DO NOT REPORT THIS TO Fadah SUPPORT, THIS IS NOT A BUG, THIS IS A CONFIGURATION PROBLEM.
                    """.stripIndent().formatted(expression)
            );
            return def;
        } catch (Exception e) {
            Fadah.getConsole().log(Level.SEVERE,
                    """
                    Unable to process expression: '%s'
                    (Report this to Fadah support)
                    """.stripIndent().formatted(expression),
                    e);
            return def;
        }
    }

    private static String escape(String input) {
        input = input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("`", "\\`");
        return input;
    }
}
