package info.preva1l.fadah.hooks.impl.permissions;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

/**
 * Created on 27/03/2025
 *
 * @author Preva1l
 */
public class LuckPermsHook extends PermissionsHook {
    private LuckPerms luckPerms;

    /**
     * Sets the permission retriever to the luckperms implementation
     * <p>
     *      The luckperms implementation first sorts by group weight, if the user is in no groups, it just uses the users base permissions
     * </p>
     *
     * @return true if luckperms api was successful
     */
    public boolean onStart() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider == null) return false;
        luckPerms = provider.getProvider();

        QueryOptions options = QueryOptions.defaultContextualOptions();

        setRetriever((perm, player) ->
                getLPUser(player)
                        .map(user -> user.getInheritedGroups(options)
                                .stream()
                                .max(Comparator.comparingInt(g -> g.getWeight().orElse(0)))
                                .flatMap(group -> extractFromNodes(perm, group.getNodes()))
                                .or(() -> extractFromNodes(perm, user.resolveInheritedNodes(options)))
                                .orElse(perm.defaultValue)
                        ).orElse(perm.defaultValue)
        );

        return true;
    }

    private Optional<Number> extractFromNodes(Permission perm, Collection<Node> nodes) {
        String nodePrefix = perm.nodePrefix;

        return nodes.stream()
                .filter(Node::getValue)
                .filter(n -> n.getKey().startsWith(nodePrefix))
                .filter(p -> canParse(p, nodePrefix))
                .map(p -> parseNumber(p.getKey().substring(nodePrefix.length())))
                .max(perm.findHighest ? compareNumbers() : compareNumbers().reversed());
    }

    private Optional<User> getLPUser(Player player) {
        return Optional.ofNullable(luckPerms.getUserManager().getUser(player.getUniqueId()));
    }

    private boolean canParse(@NotNull Node permission, @NotNull String nodePrefix) {
        try {
            parseNumber(permission.getKey().substring(nodePrefix.length()));
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }
}
