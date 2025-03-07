package info.preva1l.fadah.api;

import info.preva1l.fadah.records.listing.Listing;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Setter
public final class ListingCreateEvent extends Event implements Cancellable {
    @Getter private static final HandlerList handlerList = new HandlerList();
    private final @Nullable Player player;
    @Getter private final Listing listing;
    @Getter private boolean cancelled = false;
    @Getter private String cancelReason = "A 3rd Party Hook has cancelled the creation of this listing!";

    public ListingCreateEvent(@Nullable Player who, @NotNull Listing listing) {
        super();
        this.player = who;
        this.listing = listing;
    }

    public @Nullable Player getPlayer() {
        return player;
    }

    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }
}
