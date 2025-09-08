package info.preva1l.fadah.warnings;

import java.util.List;
import java.util.logging.Logger;

/**
 * This is a class.
 *
 * @author Preva1l
 * @since 8/09/2025
 */
public interface Warning {
    List<String> message();

    default void warn() {
        message().forEach(Logger.getLogger("Fadah")::severe);
    }
}
