package info.preva1l.fadah.records.listing;

import info.preva1l.fadah.config.Categories;
import info.preva1l.fadah.records.Category;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Created on 31/03/2025
 *
 * @author Preva1l
 */
@Getter
public abstract class BaseListing implements Listing {
    final @NotNull UUID id;
    final @NotNull UUID owner;
    final @NotNull String ownerName;
    final @NotNull ItemStack itemStack;
    String categoryID;
    final @NotNull String currencyId;
    final double tax;
    final long creationDate;
    final long deletionDate;

    /**
     * The constructor for a listing.
     *
     * @param id the listing's id. (should be randomly generated)
     * @param owner the owner of the listing.
     * @param ownerName the listing owners name.
     * @param itemStack the item being sold.
     * @param currency the currency the listing was posted with.
     * @param tax the percentage the listing owner will be taxed on the sale
     * @param creationDate epoch timestamp of when the listing was created.
     * @param deletionDate epoch timestamp of when the listing will expire.
     */
    protected BaseListing(@NotNull UUID id, @NotNull UUID owner, @NotNull String ownerName,
                      @NotNull ItemStack itemStack, @NotNull String currency,
                      double tax, long creationDate, long deletionDate) {
        this.id = id;
        this.owner = owner;
        this.ownerName = ownerName;
        this.itemStack = itemStack;
        this.currencyId = currency;
        this.tax = tax;
        this.creationDate = creationDate;
        this.deletionDate = deletionDate;
        this.categoryID = Categories.getCategoryForItem(itemStack);
    }

    @Override
    public @NotNull String getCategoryID() {
        return categoryID;
    }

    @Override
    public Category getCategory() {
        Category category = Categories.getCategory(categoryID).orElse(null);
        if (category == null) {
            categoryID = Categories.getCategoryForItem(itemStack);
            category = Categories.getCategory(categoryID).orElseThrow();
        }

        return category;
    }
}
