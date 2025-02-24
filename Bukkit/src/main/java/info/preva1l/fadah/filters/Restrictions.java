package info.preva1l.fadah.filters;

import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.processor.JavaScriptProcessor;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.CompletableFuture;

public class Restrictions {
    public static CompletableFuture<Boolean> isRestrictedItem(ItemStack item) {
        return CompletableFuture.supplyAsync(() -> {
            for (String blacklist : Config.i().getBlacklists()) {
                boolean result = JavaScriptProcessor.process(blacklist, true, item); // restrict on failure just in case
                if (result) {
                    return true;
                }
            }
            return false;
        }, DatabaseManager.getInstance().getThreadPool());
    }
}