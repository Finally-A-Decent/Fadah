package info.preva1l.fadah.filters;

import info.preva1l.fadah.utils.Text;
import info.preva1l.trashcan.flavor.annotations.Configure;
import info.preva1l.trashcan.flavor.annotations.Service;
import info.preva1l.trashcan.flavor.annotations.inject.Inject;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static info.preva1l.fadah.filters.MatcherArgType.INTEGER;
import static info.preva1l.fadah.filters.MatcherArgType.STRING;

/**
 * Created on 20/02/2025
 *
 * @author Preva1l
 */
@Service(priority = 20)
public final class MatcherService {
    public static final MatcherService instance = new MatcherService();

    private static final Pattern COMPARISON_PATTERN = Pattern.compile(
            "\"([^\"]*?)\"\\s*(==|!=|contains|startsWith|endsWith|matches)\\s*\"([^\"]*?)\"|(\\d+(?:\\.\\d+)?)\\s*(==|!=|>|<|>=|<=)\\s*(\\d+(?:\\.\\d+)?)"
    );

    @Inject private Logger logger;

    @Configure
    public void configure() {
        MatcherArgsRegistry.register(STRING, "material", item -> item.getType().toString());

        MatcherArgsRegistry.register(STRING, "name", Text::extractItemNameToString);

        MatcherArgsRegistry.register(INTEGER, "amount", item -> String.valueOf(item.getAmount()));

        MatcherArgsRegistry.register(STRING, "lore", item -> {
            var lore = item.getItemMeta().getLore();
            if (lore == null) lore = new ArrayList<>();
            return String.join("\\n", lore);
        });

        MatcherArgsRegistry.register(STRING, "model", item -> Objects.requireNonNullElse(item.getItemMeta().getItemModel(), NamespacedKey.fromString("empty:empty")).asString());
    }

    /**
     * Process an expression that results in a boolean.
     */
    public boolean process(String expression, boolean def, @Nullable ItemStack item) {
        if (item == null) return def;

        try {
            // check for simple boolean values first
            String trimmed = expression.trim();
            if ("true".equalsIgnoreCase(trimmed)) return true;
            if ("false".equalsIgnoreCase(trimmed)) return false;

            String processedExpression = replacePlaceholders(expression, item);

            // handle logical operators
            if (processedExpression.contains("&&") || processedExpression.contains("||")) return evaluateLogicalExpression(processedExpression, def);

            // try parse as a simple comparison
            return evaluateComparison(processedExpression, def);
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

    private boolean evaluateComparison(String processedExpression, boolean def) {
        Matcher matcher = COMPARISON_PATTERN.matcher(processedExpression);
        if (!matcher.matches()) {
            error(processedExpression, "Invalid comparison expression!");
            return def;
        }

        try {
            // string comparison (group 1-3)
            if (matcher.group(1) != null) {
                String leftValue = matcher.group(1);
                String operator = matcher.group(2);
                String rightValue = matcher.group(3);

                return compareStrings(leftValue, operator, rightValue);
            }

            // number comparison (group 4-6)
            if (matcher.group(4) != null) {
                String leftValue = matcher.group(4);
                String operator = matcher.group(5);
                String rightValue = matcher.group(6);

                return compareNumbers(processedExpression, leftValue, operator, rightValue);
            }

        } catch (Exception e) {
            error(processedExpression, e.getMessage());
        }

        return def;
    }

    private boolean compareStrings(String actual, String operator, String expected) {
        return switch (operator) {
            case "==" -> actual.equals(expected);
            case "!=" -> !actual.equals(expected);
            case "contains" -> actual.contains(expected);
            case "startsWith" -> actual.startsWith(expected);
            case "endsWith" -> actual.endsWith(expected);
            case "matches" -> actual.matches(expected);
            default -> false;
        };
    }

    private boolean compareNumbers(String expression, String leftStr, String operator, String rightStr) {
        try {
            double left = Double.parseDouble(leftStr);
            double right = Double.parseDouble(rightStr);

            return switch (operator) {
                case "==" -> left == right;
                case "!=" -> left != right;
                case ">" -> left > right;
                case "<" -> left < right;
                case ">=" -> left >= right;
                case "<=" -> left <= right;
                default -> false;
            };
        } catch (NumberFormatException e) {
            error(expression, "Invalid number: " + leftStr + " or " + rightStr);
            return false;
        }
    }

    private boolean evaluateLogicalExpression(String processedExpression, boolean defaultValue) {
        // handle AND operations
        if (processedExpression.contains("&&")) {
            String[] parts = splitLogicalExpression(processedExpression, "&&");
            for (String part : parts) {
                String trimmedPart = part.trim();

                // check for simple boolean first
                if ("true".equalsIgnoreCase(trimmedPart)) continue;
                if ("false".equalsIgnoreCase(trimmedPart)) return false;

                if (!evaluateComparison(trimmedPart, defaultValue)) {
                    return false;
                }
            }
            return true;
        }

        // handle OR operations
        if (processedExpression.contains("||")) {
            String[] parts = splitLogicalExpression(processedExpression, "||");
            for (String part : parts) {
                String trimmedPart = part.trim();

                // check for simple boolean first
                if ("true".equalsIgnoreCase(trimmedPart)) return true;
                if ("false".equalsIgnoreCase(trimmedPart)) continue;


                if (evaluateComparison(trimmedPart, defaultValue)) return true;
            }
            return false;
        }

        return defaultValue;
    }

    private String[] splitLogicalExpression(String expression, String operator) {
        return expression.split(Pattern.quote(operator));
    }

    private String replacePlaceholders(String expression, ItemStack item) {
        String result = expression;
        for (MatcherArg replacement : MatcherArgsRegistry.get()) {
            if (result.contains(replacement.placeholder())) {
                String value = replacement.parse(item);
                // for string types, wrap in quotes; for others, use raw value
                if (replacement.type() == MatcherArgType.STRING) {
                    value = "\"" + escape(value) + "\"";
                } else {
                    value = escape(value);
                }
                result = result.replace(replacement.placeholder(), value);
            }
        }
        return result;
    }

    private String escape(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("`", "\\`")
                .replaceAll("%[^%]+%", "\"UNKNOWN_VALUE\"");
    }

    private void error(String expression, String reason) {
        logger.severe(
                """
                Unable to process expression: '%s'
                (%s)
                
                This is likely related to a category matcher or a item blacklist.
                DO NOT REPORT THIS TO Fadah SUPPORT, THIS IS NOT A BUG, THIS IS A CONFIGURATION PROBLEM.
                Refer: https://docs.preva1l.info/fadah/setup/category-filtering-and-blacklists/
                """.stripIndent().formatted(expression, reason)
        );
    }
}