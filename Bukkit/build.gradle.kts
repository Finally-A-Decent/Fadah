import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("de.eldoria.plugin-yml.paper") version "0.7.1"
}

repositories {
    maven(url = "https://repo.auxilor.io/repository/maven-public/")
    maven(url = "https://oss.sonatype.org/content/groups/public/")
    maven(url = "https://jitpack.io")
    maven(url = "https://repo.codemc.io/repository/maven-snapshots/")
    maven(url = "https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven(url = "https://mvn-repo.arim.space/lesser-gpl3/")
    maven(url = "https://repo.rosewooddev.io/repository/public/")
    maven(url = "https://nexus.neetgames.com/repository/maven-releases/")
}

dependencies {
    implementation(project(":API"))
    compileOnly("io.papermc.paper:paper-api:1.19-R0.1-SNAPSHOT")

    // Trashcan
    annotationProcessor("info.preva1l.trashcan:paper:1.0.0")
    implementation("info.preva1l.trashcan:paper:1.0.0")

    compileOnly("me.clip:placeholderapi:2.11.6") // Placeholder support

    // Database
    library("com.zaxxer:HikariCP:6.3.0")
    library("org.xerial:sqlite-jdbc:3.49.1.0")
    library("com.mysql:mysql-connector-j:9.2.0")
    library("org.mariadb.jdbc:mariadb-java-client:3.5.3")
    library("org.mongodb:mongodb-driver-sync:5.4.0")

    library("net.wesjd:anvilgui:1.10.4-SNAPSHOT") // Search Menu

    library("net.kyori:adventure-text-serializer-gson:4.20.0")

    library("org.mozilla:rhino:1.8.0")

    // Currency
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("com.github.Emibergo02:RedisEconomy:4.3.9")
    compileOnly(files("../libs/CoinsEngine-2.3.5.jar"))
    compileOnly("org.black_ixx:playerpoints:3.2.0")

    // Eco Items
    compileOnly("com.willfp:libreforge:4.58.1")
    compileOnly("com.willfp:eco:6.56.0")
    compileOnly("com.willfp:EcoItems:5.43.1")
    compileOnly("com.influxdb:influxdb-client-java:7.2.0") // InfluxDB logging
    compileOnly("net.luckperms:api:5.4") // Permissions enhancement
    compileOnly("com.gmail.nossr50.mcMMO:mcMMO:2.2.004") {
        isTransitive = false
    }

    // Migrators
    compileOnly("com.github.Maxlego08:zAuctionHouseV3-API:3.2.1.9") // zAuctionHouse
    compileOnly(files("../libs/AuctionHouse-1.20.4-3.7.1.jar")) // AuctionHouse
    compileOnly(files("../libs/AkarianAuctionHouse-1.3.1-b6.jar")) // AkarianAuctionHouse
}

tasks.withType<ShadowJar> {
    relocate("com.github.puregero.multilib", "info.preva1l.fadah.libs.multilib")
    relocate("info.preva1l.hooker", "info.preva1l.fadah.hooks.lib")
    relocate("info.preva1l.trashcan", "info.preva1l.fadah.trashcan")
}

paper {
    name = "Fadah"
    version = rootProject.version.toString()
    description = "Fadah (Finally a Decent Auction House) is the fast, modern and advanced auction house plugin that you have been looking for!"
    website = "https://docs.preva1l.info/"
    author = "Preva1l"
    main = rootProject.group.toString() + ".Fadah"
    loader = "info.preva1l.fadah.trashcan.plugin.libloader.BaseLibraryLoader"
    generateLibrariesJson = true
    foliaSupported = true
    apiVersion = "1.19"

    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD

    serverDependencies {
        listOf(
            "PlaceholderAPI",
            "EcoItems",
            "LuckPerms",
            "zAuctionHouseV3",
            "AuctionHouse",
            "RedisEconomy",
            "CoinsEngine",
            "Vault",
            "mcMMO"
        ).forEach {
            register(it) {
                load = PaperPluginDescription.RelativeLoadOrder.AFTER
                required = false
            }
        }
    }

    permissions {
        register("fadah.max-listings.<amount>") {
            description = "The amount of items a player can list at any one time."
        }

        register("fadah.listing-tax.<amount>") {
            description = "The percentage a player will get taxed when creating a listing."
        }

        register("fadah.advert-price.<amount>") {
            description = "The cost of a listing advertisement."
            default = BukkitPluginDescription.Permission.Default.TRUE
        }

        register("fadah.use") {
            default = BukkitPluginDescription.Permission.Default.TRUE
        }

        register("fadah.collection-box") {
            default = BukkitPluginDescription.Permission.Default.TRUE
        }

        register("fadah.expired-items") {
            default = BukkitPluginDescription.Permission.Default.TRUE
        }

        register("fadah.help") {
            default = BukkitPluginDescription.Permission.Default.TRUE
        }

        register("fadah.profile") {
            default = BukkitPluginDescription.Permission.Default.TRUE
        }

        register("fadah.active-listings") {
            default = BukkitPluginDescription.Permission.Default.TRUE
        }

        register("fadah.watch") {
            default = BukkitPluginDescription.Permission.Default.TRUE
        }

        register("fadah.search") {
            default = BukkitPluginDescription.Permission.Default.TRUE
        }

        register("fadah.view") {
            default = BukkitPluginDescription.Permission.Default.TRUE
        }

        register("fadah.manage.*") {
            children = listOf(
                "fadah.manage.profile",
                "fadah.manage.active-listings",
                "fadah.manage.expired-items",
                "fadah.manage.collection-box"
            )
        }
    }
}