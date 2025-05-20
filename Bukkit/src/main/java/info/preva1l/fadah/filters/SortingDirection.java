package info.preva1l.fadah.filters;

import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.utils.Text;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;

@AllArgsConstructor
public enum SortingDirection {
    ASCENDING(
            Lang.i().getSort().getName().getAscending(),
            Lang.i().getSort().getAge().getAscending(),
            Lang.i().getSort().getPrice().getAscending(),
            Lang.i().getSort().getMode().getAscending()
    ),
    DESCENDING(
            Lang.i().getSort().getName().getDescending(),
            Lang.i().getSort().getAge().getDescending(),
            Lang.i().getSort().getPrice().getDescending(),
            Lang.i().getSort().getMode().getDescending()
    ),
    ;
    private final String alphaName;
    private final String ageName;
    private final String priceName;
    private final String modeName;

    public Component getAlphaName() {
        return Text.text(alphaName);
    }

    public Component getAgeName() {
        return Text.text(ageName);
    }

    public Component getPriceName() {
        return Text.text(priceName);
    }

    public Component getModeName() {
        return Text.text(modeName);
    }
}
