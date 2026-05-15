package info.preva1l.fadah.hooks.impl;

import com.gmail.nossr50.api.AbilityAPI;
import info.preva1l.fadah.commands.subcommands.SellSubCommand;
import info.preva1l.fadah.config.Lang;

/**
 * Created on 28/04/2025
 *
 * @author Preva1l
 */
public class McMMOHook {
    public void start() {
        SellSubCommand.restrictions.add(player -> {
            if (AbilityAPI.isAnyAbilityEnabled(player)) {
                Lang.sendMessage(player, Lang.i().getErrors().getMcmmoBlocking());
                return true;
            }

            return false;
        });
    }
}
