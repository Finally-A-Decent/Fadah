package info.preva1l.fadah.utils.guis;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.function.Consumer;

/**
 * Simple {@link ItemStack} builder.
 *
 * @author MrMicky
 * @author Preva1l
 */
@SuppressWarnings({"unused", "deprecation"})
public class ItemBuilder {

    private final ItemStack item;

    public ItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    public ItemBuilder(ItemStack item) {
        this.item = Objects.requireNonNull(item, "item");
    }

    public static ItemBuilder copyOf(ItemStack item) {
        return new ItemBuilder(item.clone());
    }

    public ItemBuilder edit(Consumer<ItemStack> function) {
        function.accept(this.item);
        return this;
    }

    public ItemBuilder meta(Consumer<ItemMeta> metaConsumer) {
        return edit(item -> {
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                metaConsumer.accept(meta);
                item.setItemMeta(meta);
            }
        });
    }

    public <T extends ItemMeta> ItemBuilder meta(Class<T> metaClass, Consumer<T> metaConsumer) {
        return meta(meta -> {
            if (metaClass.isInstance(meta)) {
                metaConsumer.accept(metaClass.cast(meta));
            }
        });
    }

    public ItemBuilder type(Material material) {
        return edit(item -> item.setType(material));
    }

    public ItemBuilder durability(int data) {
        return durability((short) data);
    }

    @SuppressWarnings("deprecation")
    public ItemBuilder durability(short durability) {
        return edit(item -> item.setDurability(durability));
    }

    public ItemBuilder amount(int amount) {
        return edit(item -> item.setAmount(amount));
    }

    public ItemBuilder enchant(Enchantment enchantment) {
        return enchant(enchantment, 1);
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        return meta(meta -> meta.addEnchant(enchantment, level, true));
    }

    public ItemBuilder removeEnchant(Enchantment enchantment) {
        return meta(meta -> meta.removeEnchant(enchantment));
    }

    public ItemBuilder removeEnchants() {
        return meta(m -> m.getEnchants().keySet().forEach(m::removeEnchant));
    }

    public ItemBuilder glow(boolean glow) {
        return enchant(Enchantment.getByKey(NamespacedKey.minecraft("unbreaking")));
    }

    public ItemBuilder name(String name) {
        return meta(meta -> meta.setDisplayName(name));
    }

    public ItemBuilder name(Component component) {
        return meta(meta -> meta.displayName(component));
    }

    public ItemBuilder lore(String lore) {
        return lore(Collections.singletonList(lore));
    }

    public ItemBuilder lore(String... lore) {
        return lore(Arrays.asList(lore));
    }

    public ItemBuilder lore(List<String> lore) {
        return meta(meta -> meta.setLore(lore));
    }

    public ItemBuilder lore(Collection<Component> lore) {
        return meta(meta -> meta.lore(new ArrayList<>(lore)));
    }

    public void addLore(String line) {
        meta(meta -> {
            List<String> lore = meta.getLore();

            if (lore == null) {
                meta.setLore(Collections.singletonList(line));
                return;
            }

            lore.add(line);
            meta.setLore(lore);
        });
    }

    public void addLore(Component line) {
        meta(meta -> {
            List<Component> lore = meta.lore();

            if (lore == null) {
                meta.lore(Collections.singletonList(line));
                return;
            }

            lore.add(line);
            meta.lore(lore);
        });
    }

    public ItemBuilder addLore(String... lines) {
        return addLore(Arrays.asList(lines));
    }

    public ItemBuilder addLore(List<String> lines) {
        return meta(meta -> {
            List<String> lore = meta.getLore();

            if (lore == null) {
                meta.setLore(lines);
                return;
            }

            lore.addAll(lines);
            meta.setLore(lore);
        });
    }

    public ItemBuilder addLore(Collection<Component> lines) {
        return meta(meta -> {
            List<Component> lore = meta.lore();

            if (lore == null) {
                meta.lore(new ArrayList<>(lines));
                return;
            }

            lore.addAll(lines);
            meta.lore(lore);
        });
    }

    public ItemBuilder flags(ItemFlag... flags) {
        return meta(meta -> {
            for (ItemFlag flag : flags) {
                meta.addItemFlags(flag);
            }
        });
    }

    public ItemBuilder flags() {
        return flags(ItemFlag.values());
    }

    public ItemBuilder removeFlags(ItemFlag... flags) {
        return meta(meta -> meta.removeItemFlags(flags));
    }

    public ItemBuilder removeFlags() {
        return removeFlags(ItemFlag.values());
    }

    @SneakyThrows
    public ItemBuilder attributeSillyStuff() {
        for (Attribute attribute : (Attribute[]) Attribute.class.getDeclaredMethod("values").invoke(null)) {
            meta(meta -> meta.setAttributeModifiers(ImmutableListMultimap.of()));
        }
        return this;
    }

    public ItemBuilder setAttributes(Multimap<Attribute,AttributeModifier> map) {
        return meta(meta -> meta.setAttributeModifiers(map));
    }

    public ItemBuilder armorColor(Color color) {
        return meta(LeatherArmorMeta.class, meta -> meta.setColor(color));
    }

    public ItemStack build() {
        return this.item;
    }

    public ItemBuilder skullOwner(OfflinePlayer player) {
        if (item.getType() != Material.PLAYER_HEAD) return this;
        SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
        skullMeta.setOwningPlayer(player);
        item.setItemMeta(skullMeta);

        return edit(item -> item.setItemMeta(skullMeta));
    }

    public ItemBuilder modelData(int modelData) {
        return meta(meta -> meta.setCustomModelData(modelData));
    }
}