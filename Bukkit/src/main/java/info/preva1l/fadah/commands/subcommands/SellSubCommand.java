package info.preva1l.fadah.commands.subcommands;

import com.github.puregero.multilib.MultiLib;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.data.PermissionsData;
import info.preva1l.fadah.guis.NewListingMenu;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SellSubCommand extends SubCommand {
    public static List<UUID> running = new ArrayList<>();

    public SellSubCommand(Fadah plugin) {
        super(plugin, Lang.i().getCommands().getSell().getAliases(), Lang.i().getCommands().getSell().getDescription());
    }

    @SubCommandArgs(name = "sell", permission = "fadah.use")
    public void execute(@NotNull SubCommandArguments command) {
        if (!Config.i().isEnabled()) {
            command.reply(Lang.i().getPrefix() + Lang.i().getErrors().getDisabled());
            return;
        }
        assert command.getPlayer() != null;
        if (running.contains(command.getPlayer().getUniqueId())) return;
        running.add(command.getPlayer().getUniqueId());
        if (command.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) {
            command.reply(Lang.i().getPrefix() + Lang.i().getCommands().getSell().getMustHoldItem());
            running.remove(command.getPlayer().getUniqueId());
            return;
        }
        if (command.args().length == 0) {
            command.reply(Lang.i().getPrefix() + Lang.i().getErrors().getInvalidUsage()
                    .replace("%command%", Lang.i().getCommands().getSell().getUsage()));
            running.remove(command.getPlayer().getUniqueId());
            return;
        }
        String priceString = command.args()[0];

        if (priceString.toLowerCase().contains("nan")) {
            command.reply(Lang.i().getPrefix() + Lang.i().getCommands().getSell().getMustBeNumber());
            running.remove(command.getPlayer().getUniqueId());
            return;
        }

        double price = StringUtils.getAmountFromString(priceString);

        try {
            if (price < Config.i().getListingPrice().getMin()) {
                command.reply(Lang.i().getPrefix() + Lang.i().getCommands().getSell().getListingPrice().getMin()
                        .replace("%price%", Config.i().getListingPrice().getMin() + ""));
                running.remove(command.getPlayer().getUniqueId());
                return;
            }
            if (price > Config.i().getListingPrice().getMax()) {
                command.reply(Lang.i().getPrefix() + Lang.i().getCommands().getSell().getListingPrice().getMax()
                        .replace("%price%", Config.i().getListingPrice().getMax() + ""));
                running.remove(command.getPlayer().getUniqueId());
                return;
            }
            int currentListings = PermissionsData.getCurrentListings(command.getPlayer());
            int maxListings = PermissionsData.getHighestInt(PermissionsData.PermissionType.MAX_LISTINGS, command.getPlayer());
            if (currentListings >= maxListings) {
                command.reply(Lang.i().getPrefix() + Lang.i().getCommands().getSell().getMaxListings()
                        .replace("%current%", currentListings + "")
                        .replace("%max%", maxListings + ""));
                running.remove(command.getPlayer().getUniqueId());
                return;
            }
            MultiLib.getEntityScheduler(command.getPlayer()).run(Fadah.getINSTANCE(),
                    t -> new NewListingMenu(command.getPlayer(), price).open(command.getPlayer()),
                    () -> command.reply("&A critical error has occurred while trying to obtain the correct thread."));

        } catch (NumberFormatException e) {
            command.reply(Lang.i().getPrefix() + Lang.i().getCommands().getSell().getMustBeNumber());
            running.remove(command.getPlayer().getUniqueId());
        }
    }
}
