package info.preva1l.fadah.utils.guis;

import info.preva1l.fadah.config.misc.Tuple;
import info.preva1l.fadah.utils.Text;
import info.preva1l.fadah.utils.config.BasicConfig;
import info.preva1l.fadah.utils.config.LanguageConfig;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

public record GuiLayout(
        @NotNull LayoutManager.MenuType menuType,
        @NotNull List<Integer> fillerSlots,
        @NotNull List<Integer> paginationSlots,
        @NotNull List<Integer> scrollbarSlots,
        @NotNull List<Integer> noItems,
        @NotNull HashMap<LayoutManager.ButtonType, Integer> buttonSlots,
        @NotNull String guiTitle,
        int guiSize,
        @NotNull LanguageConfig language,
        @NotNull BasicConfig extraConfig
) {
    @SafeVarargs
    public final Component formattedTitle(Tuple<String, Object>... args) {
        return Text.text(guiTitle, args);
    }
}
