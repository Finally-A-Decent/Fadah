package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.context.CommandContext;

/**
 * Created on 31/03/2025
 *
 * @author Preva1l
 */
public interface AboutSubCommand {
    default void about(CommandContext<CommandSender> ctx) {
        TextColor primary = TextColor.fromHexString("#9555FF");
        TextColor secondary = TextColor.fromHexString("#bba4e0");
        Component about = Component.text()
                .append(Component.text("Finally a Decent Auction House", primary, TextDecoration.BOLD))
                .append(Component.newline())
                .append(Component.text("Fadah is the fast, modern and advanced auction house plugin that you have been looking for!", secondary))
                .append(Component.newline())
                .append(Component.text("Version: " + Fadah.getInstance().getCurrentVersion(), secondary))
                .append(Component.newline())
                .append(Component.text("Author: Preva1l", secondary))
                .append(Component.newline())
                .append(Component.text("Docs: https://docs.preva1l.info/fadah/", secondary))
                .append(Component.newline())
                .append(Component.text("Support: https://discord.gg/4KcF7S94HF", secondary))
                .build();
        ctx.sender().sendMessage(about);
    }
}
