package info.preva1l.fadah.commands.subcommands;

import info.preva1l.trashcan.commands.bukkit.CommandPermission;

/**
 * Created on 31/03/2025
 *
 * @author Preva1l
 */
public record SubCommandInfo(
        String name,
        String description,
        CommandPermission permission
) {}
