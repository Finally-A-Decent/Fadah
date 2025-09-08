import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import info.preva1l.trashcan.description.paper.Permission
import info.preva1l.trashcan.description.paper.PluginLoadOrder
import info.preva1l.trashcan.description.paper.dependency
import info.preva1l.trashcan.setRemapped
import info.preva1l.trashcan.trashcan
import info.preva1l.trashcan.description.paper.PaperDependencyDefinition.RelativeLoadOrder as RLO

plugins {
    fadah.common
}

trashcan {
    paper = true
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
    trashcan()

    library(libs.bundles.databases)
    library(libs.redisson)

    library(libs.multilib)
    library(libs.anvilgui) { setRemapped(true) }
    library(libs.adventure.gson)
    library(libs.influxdb)

    dependency(libs.placeholderapi(), "PlaceholderAPI") { load = RLO.BEFORE ; required = false }
    dependency(libs.luckperms(), "LuckPerms") { load = RLO.BEFORE ; required = false }
    compileOnly(libs.mcmmo) { isTransitive = false }

    // Currency
    dependency(libs.vault(), "Vault") { load = RLO.BEFORE ; required = false }
    dependency(libs.rediseconomy(), "RedisEconomy") { load = RLO.BEFORE ; required = false }
    dependency(files("../libs/CoinsEngine-2.3.5.jar"), "CoinsEngine") { load = RLO.BEFORE ; required = false }
    dependency(libs.playerpoints(), "PlayerPoints") { load = RLO.BEFORE ; required = false }

    // Eco Items
    compileOnly(libs.bundles.eco) { isTransitive = false }
    dependency(libs.eco.items(), "EcoItems") { load = RLO.BEFORE ; required = false }

    dependency(libs.zauctionhouse(), "zAuctionHouseV3") { required = false }
    dependency(files("../libs/AuctionHouse-1.20.4-3.7.1.jar"), "AuctionHouse") { required = false }
    compileOnly(files("../libs/AkarianAuctionHouse-1.3.1-b6.jar"))
}

tasks.withType<ShadowJar> {
    relocate("info.preva1l.hooker", "info.preva1l.fadah.hooks.lib")
    relocate("info.preva1l.trashcan", "info.preva1l.fadah.trashcan")
}

paper {
    description = "Fadah (Finally a Decent Auction House) is the fast, modern and advanced auction house plugin that you have been looking for!"
    website = "https://docs.preva1l.info/fadah/"
    author = "Preva1l"
    main = rootProject.group.toString() + ".Fadah"
    loader = "info.preva1l.fadah.trashcan.extension.libloader.BaseLibraryLoader"
    foliaSupported = true
    apiVersion = "1.21"

    load = PluginLoadOrder.POSTWORLD

    dependencies {
        serverDependencies.register("mcMMO") { load = RLO.BEFORE ; required = false }
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
            default = Permission.Default.TRUE
        }

        listOf(
            "fadah.use",
            "fadah.collection-box",
            "fadah.expired-items",
            "fadah.help",
            "fadah.profile",
            "fadah.active-listings",
            "fadah.watch",
            "fadah.search",
            "fadah.view",
        ).forEach {
            register(it) {
                default = Permission.Default.TRUE
            }
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

operator fun Provider<MinimalExternalModuleDependency>.invoke(): String =
    get().let { "${it.module.group}:${it.module.name}:${it.version}" }