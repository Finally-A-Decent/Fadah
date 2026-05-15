package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.filters.SortingDirection;
import info.preva1l.fadah.filters.SortingMethod;
import info.preva1l.fadah.guis.ViewListingsMenu;
import info.preva1l.fadah.utils.Tasks;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Created on 31/03/2025
 *
 * @author Preva1l
 */
public interface InspectSubCommand {
    default void inspect(
            Player player,
            OfflinePlayer owner,
            @Nullable String search,
            @Nullable SortingMethod sort,
            @Nullable SortingDirection direction) {
        Tasks.sync(Fadah.getInstance(), player, () ->
                new ViewListingsMenu(
                        player,
                        owner,
                        search,
                        sort,
                        direction
                ).open(player), () -> {});
    }
}
