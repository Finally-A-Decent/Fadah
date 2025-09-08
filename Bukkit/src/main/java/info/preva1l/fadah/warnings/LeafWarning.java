package info.preva1l.fadah.warnings;

import java.util.List;

/**
 * This is a class.
 *
 * @author Preva1l
 * @since 8/09/2025
 */
public final class LeafWarning implements Warning {
    @Override
    public List<String> message() {
        return List.of(
                "---------------- WARNING ----------------",
                "       Fadah does not support Leaf!      ",
                " Leaf is unstable and breaks common api. ",
                "    You must use one of the following:   ",
                "    Paper, Pufferfish, Purpur, USpigot   ",
                "     ASPaper, Folia or ShreddedPaper     ",
                "     Other Paper forks may also work     ",
                "-----------------------------------------",
                "    You will not receive any support!    ",
                "---------------- WARNING ----------------"
        );
    }
}
