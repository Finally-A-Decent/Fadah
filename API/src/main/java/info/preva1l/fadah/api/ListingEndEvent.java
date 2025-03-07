package info.preva1l.fadah.api;

import info.preva1l.fadah.records.listing.Listing;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public final class ListingEndEvent extends Event {
    @Getter private static final HandlerList handlerList = new HandlerList();
    private final ListingEndReason reason;
    private final Listing listing;

    public ListingEndEvent(Listing listing, ListingEndReason reason) {
        super();
        this.listing = listing;
        this.reason = reason;
    }

    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }
}
