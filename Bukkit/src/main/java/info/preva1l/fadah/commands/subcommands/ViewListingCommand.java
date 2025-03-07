package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.guis.ConfirmPurchaseMenu;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class ViewListingCommand extends SubCommand {
    public ViewListingCommand(Fadah plugin) {
        super(plugin, Lang.i().getCommands().getViewListing().getAliases(), Lang.i().getCommands().getViewListing().getDescription());
    }

    @SubCommandArgs(name = "view-listing", permission = "fadah.use")
    public void execute(@NotNull SubCommandArguments command) {
        assert command.getPlayer() != null;
        if (command.args().length == 0) {
            command.reply(Lang.i().getPrefix() + Lang.i().getErrors().getInvalidUsage()
                    .replace("%command%", Lang.i().getCommands().getViewListing().getUsage()));
            return;
        }
        UUID listingId;
        try {
            listingId = UUID.fromString(command.args()[0]);
        } catch (IllegalArgumentException e) {
            command.reply(Lang.i().getPrefix() + Lang.i().getErrors().getInvalidUsage()
                    .replace("%command%", Lang.i().getCommands().getViewListing().getUsage()));
            return;
        }
        Optional<Listing> listing = CacheAccess.get(Listing.class, listingId);

        if (listing.isEmpty()) {
            command.reply(Lang.i().getPrefix() + Lang.i().getErrors().getDoesNotExist());
            return;
        }

        if (listing.get().isOwner(command.getPlayer())) {
            command.reply(Lang.i().getPrefix() + Lang.i().getErrors().getOwnListings());
            return;
        }

        new ConfirmPurchaseMenu(listing.get(), command.getPlayer(), null, null, null,
                null, false, null).open(command.getPlayer());
    }
}
