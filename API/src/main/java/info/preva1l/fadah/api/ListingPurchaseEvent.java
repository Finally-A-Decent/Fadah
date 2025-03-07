package info.preva1l.fadah.api;

import info.preva1l.fadah.records.listing.Listing;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public final class ListingPurchaseEvent extends Event {
    @Getter private static final HandlerList handlerList = new HandlerList();
    private final Listing listing;
    private final Player buyer;

    public ListingPurchaseEvent(Listing listing, Player buyer) {
        super();
        this.listing = listing;
        this.buyer = buyer;
    }

    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }
}
