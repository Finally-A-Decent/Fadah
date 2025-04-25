package info.preva1l.fadah.config;

import de.exlll.configlib.Configuration;
import de.exlll.configlib.NameFormatters;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.utils.Text;
import info.preva1l.trashcan.plugin.annotations.PluginReload;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Getter
@Configuration
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("FieldMayBeFinal")
public class Lang {
    private static Lang instance;

    private static final String CONFIG_HEADER = """
            #########################################
            #                  Fadah                #
            #          Language Configuration       #
            #########################################
            """;

    private static final YamlConfigurationProperties PROPERTIES = YamlConfigurationProperties.newBuilder()
            .charset(StandardCharsets.UTF_8)
            .setNameFormatter(NameFormatters.LOWER_KEBAB_CASE)
            .header(CONFIG_HEADER).build();

    private String prefix = "&#9555FF&lFadah &r";

    private String categorySelected = "&e&lSELECTED";

    private Notifications notifications = new Notifications();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Notifications {
        private List<String> advert = List.of(
                "&f--------------------------------------------------",
                "&f%player% &ehas just made a new listing on the auction house!",
                "&fItem: &e%item%",
                "&fPrice: &a$%price%",
                "&7&n<click:run_command:'/ah view-listing %listing_id%'><hover:show_text:'Click to view!'>(Click here to view the listing!)</hover></click>",
                "&f--------------------------------------------------"
        );
        private List<String> newListing = List.of(
                "&f------------------------------------------------",
                "&eYou have a successfully listed an item for sale!",
                "&fItem: &e%item%",
                "&fPrice: &a$%price%",
                "&fExpires in: &6%time%",
                "&fActive Listings: &d%current_listings%&f/&5%max_listings%",
                "&fYou have been taxed: &9%tax%% &7(&a$%price_after_tax%&7)",
                "&f------------------------------------------------"
        );
        private List<String> newItem = List.of(
                "&f------------------------------------------",
                "&eYou have a new item in your collection box!",
                "&f             /ah redeem!",
                "&f------------------------------------------"
        );
        private List<String> sale = List.of(
                "&f----------------------------------------------",
                "&eYou have sold an item on the Auction House!",
                "&fItem: &e%item%",
                "&fMoney Made: &a$%price%",
                "&fBuyer: &a$%buyer%",
                "&f----------------------------------------------"
        );
        private List<String> watched = List.of(
                "&f-------------------------------------------------------",
                "&eA listing meeting your criteria has come on the market!",
                "&fItem: &e%item%",
                "&fPrice: &a$%price%",
                "&7&n<click:run_command:'/ah view-listing %listing_id%'><hover:show_text:'Click to view!'>(Click here to view the listing!)</hover></click>",
                "&f-------------------------------------------------------"
        );
        private String cancelled = "&cListing Cancelled!";
    }

    private Commands commands = new Commands();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Commands {
        private Main main = new Main();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Main {
            private List<String> aliases = List.of("auctionhouse", "ah", "auctions", "auction");
        }

        private Sell sell = new Sell();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Sell {
            private String description = "Create a new listing on the auction house";
            private String usage = "ah sell <price>";
            private List<String> aliases = List.of("new-listing", "create-listing");

            private String mustHoldItem = "&cYou must have an item in your hand to sell!";
            private String mustBeNumber = "&cThe price must be a number!";
            private String maxListings = "&cYou have reached your max listings! (%current%/%max%)";
            private ListingPrice listingPrice = new ListingPrice();

            @Getter
            @Configuration
            @NoArgsConstructor(access = AccessLevel.PRIVATE)
            public static class ListingPrice {
                private String max = "&fPrice must be less than &a$%price%";
                private String min = "&fPrice must be at least &a$%price%";
            }
        }

        private View view = new View();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class View {
            private String description = "View another players active listings";
            private String usage = "ah view <player>";
            private List<String> aliases = List.of("visit");
        }

        private Watch watch = new Watch();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Watch {
            private String description = "Watch for listings and get notified when one gets listed";
            private List<String> aliases = List.of("listen");
        }

        private Profile profile = new Profile();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Profile {
            private String description = "View your auction profile";
            private List<String> aliases = List.of();
        }

        private ActiveListings activeListings = new ActiveListings();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class ActiveListings {
            private String description = "View your active listings";
            private List<String> aliases = List.of("active");
        }

        private ExpiredItems expiredItems = new ExpiredItems();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class ExpiredItems {
            private String description = "View your expired items";
            private List<String> aliases = List.of("expired");
        }

        private CollectionBox collectionBox = new CollectionBox();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class CollectionBox {
            private String description = "View your collection box";
            private List<String> aliases = List.of("redeem");
        }

        private History history = new History();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class History {
            private String description = "View your listing history";
            private List<String> aliases = List.of("hist");
        }

        private Help help = new Help();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Help {
            private String description = "This command!";
            private List<String> aliases = List.of();

            private String header = "&#9555FF&lAuctionHouse &eHelp";
            private String format = "&b/ah %command% &8&l| &f%description%";
        }

        private Reload reload = new Reload();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Reload {
            private String description = "Reload fadah";
            private List<String> aliases = List.of("rl");

            private String success = "&aAll configuration files reloaded successfully!";
            private String fail = "&cSome configuration files failed to reload! &7(Check console)";
            private String remote = "&aA global configuration reload has been received!";
        }

        private Toggle toggle = new Toggle();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Toggle {
            private String description = "Toggle whether people can use the auction house";
            private List<String> aliases = List.of();

            private String message = "&fFadah has been %status%&r&f!";
            private String remote = "&fAuction House has been %status%&r&f from a remote server!";
            private String enabled = "&a&lENABLED";
            private String disabled = "&c&lDISABLED";
        }

        private About about = new About();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class About {
            private String description = "Get information about fadah";
            private List<String> aliases = List.of();
        }

        private ViewListing viewListing = new ViewListing();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class ViewListing {
            private String description = "View a specific listing";
            private String usage = "ah view-listing <uuid>";
            private List<String> aliases = List.of();
        }

        private Search search = new Search();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Search {
            private String description = "Search active listings";
            private String usage = "ah search <query...> [(-cat|--category)=tools, (-p|--player)=player_name, (-sort|--sorting-method)=price, (-direction|--sorting-direction)=ascending)]";
            private List<String> aliases = List.of("s");
        }
    }

    private AdvertActions advertActions = new AdvertActions();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AdvertActions {
        private String post = "Post Advert";
        private String silent = "No Advert";
    }

    private LogActions logActions = new LogActions();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class LogActions {
        private String listingStarted = "Listing Started";
        private String listingPurchased = "Listing Purchased";
        private String listingSold = "Listing Sold";
        private String listingCancelled = "Listing Cancelled";
        private String listingExpired = "Listing Expired";
        private String expiredItemClaimed = "Expired Listing Claimed";
        private String collectionBoxClaimed = "Collection Box Item Claimed";
        private String listingCancelledAdmin = "Listing Cancelled by Admins";
        private String expiredItemClaimedAdmin = "Expired Listing Claimed by Admins";
        private String collectionBoxClaimedAdmin = "Collection Box Item Claimed by Admins";
    }

    private Sort sort = new Sort();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Sort {
        private Age age = new Age();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Age {
            private String name = "Sort By Listing Age";
            private String descending = "Oldest First";
            private String ascending = "Newest First";
        }

        private Name name = new Name();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Name {
            private String name = "Sort Alphabetically By Name";
            private String descending = "Descending (Z-A)";
            private String ascending = "Ascending (A-Z)";
        }

        private Price price = new Price();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Price {
            private String name = "Sort By Listing Price";
            private String descending = "Cheapest First (Low to High)";
            private String ascending = "Most Expensive First (High to Low)";
        }
    }

    private Errors errors = new Errors();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Errors {
        private String disabled = "&cThe Auction House is currently disabled!";
        private String commandNotFound = "&cThis command does not exist!";
        private String mustBePlayer = "&cOnly players can run this command!";
        private String restricted = "&cYou cannot sell this item!";
        private String noPermission = "&cYou do not have permission to execute this command!";
        private String playerNotFound = "&c%player% was not found!";
        private String invalidUsage = "&cUsage: /%command%";
        private String invalidArgument = "&cArgument %arg% is not the required type of %type%!";
        private String doesNotExist = "&cThis listing does not exist!";
        private String ownListings = "&cYou cannot buy your own listing!";
        private String tooExpensive = "&cYou cannot afford this item!";
        private String inventoryFull = "&cYou don't have any free room in your inventory!";
        private String advertExpense = "&cYour advert failed to post because you did not have enough money!";
        private String databaseLoading = "&cDatabase not connected! Please Wait";
        private String cooldown = "&cPlease wait &f%time%&c!";
        private String other = "&cListing this item failed! (%ex%)";
        private String alreadyHighestBidder = "&cYou are already the highest bidder!";
        private String bidTooLow = "&cThe current bid is higher than your bid!";
    }

    private Words words = new Words();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Words {
        private String your = "your";
        private String none = "None";
        private String startingBid = "Starting Bid";
        private Modes modes = new Modes();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Modes {
            private String buyItNow = "BIN";
            private String bidding = "Bidding";
        }
    }

    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(Text.text(sender instanceof Player player ? player : null, message));
    }

    public void save() {
        YamlConfigurations.save(new File(Fadah.getInstance().getDataFolder(), "lang.yml").toPath(), Lang.class, this);
    }

    @PluginReload
    public static void reload() {
        instance = YamlConfigurations.load(new File(Fadah.getInstance().getDataFolder(), "lang.yml").toPath(), Lang.class, PROPERTIES);
    }

    public static Lang i() {
        if (instance != null) {
            return instance;
        }

        return instance = YamlConfigurations.update(new File(Fadah.getInstance().getDataFolder(), "lang.yml").toPath(), Lang.class, PROPERTIES);
    }
}