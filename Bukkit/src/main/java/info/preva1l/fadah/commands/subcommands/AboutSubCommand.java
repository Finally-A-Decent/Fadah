package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.william278.desertwell.about.AboutMenu;
import org.jetbrains.annotations.NotNull;

public class AboutSubCommand extends SubCommand {
    private final BukkitAudiences bukkitAudiences;
    public AboutSubCommand(Fadah plugin) {
        super(plugin, Lang.i().getCommands().getAbout().getAliases(), Lang.i().getCommands().getAbout().getDescription());
        this.bukkitAudiences = BukkitAudiences.builder(plugin).build();
    }

    @SubCommandArgs(name = "about", inGameOnly = false)
    public void execute(@NotNull SubCommandArguments command) {
        final AboutMenu aboutMenu = AboutMenu.builder()
                .title(Component.text("Finally a Decent Auction House"))
                .description(Component.text("Fadah is the fast, modern and advanced auction house plugin that you have been looking for!"))
                .credits("Author",
                        AboutMenu.Credit.of("Preva1l")
                                .description("Click to visit website").url("https://docs.preva1l.info/"))
                .credits("Contributors",
                        AboutMenu.Credit.of("WuzzyLV"),
                        AboutMenu.Credit.of("asdevjava"),
                        AboutMenu.Credit.of("InvadedLands")
                        )
                .buttons(AboutMenu.Link.of("https://discord.gg/4KcF7S94HF").text("Discord Support").icon("⭐"))
                .themeColor(TextColor.fromHexString("#9555FF"))
                .secondaryColor(TextColor.fromHexString("#bba4e0"))
                .build();
        bukkitAudiences.sender(command.sender()).sendMessage(aboutMenu.toComponent());
    }
}
