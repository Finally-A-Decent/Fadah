import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    fadah.common
}

repositories {
    maven(url = "https://repo.clojars.org/")
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

    implementation(libs.bundles.databases)
    implementation(libs.redisson)

    implementation(libs.multilib)
    implementation(libs.anvilgui)
    implementation(libs.adventure.gson)
    implementation(libs.influxdb)
    implementation("de.exlll:configlib-yaml:4.5.0")
    implementation("org.incendo:cloud-annotations:2.0.0")
    implementation("org.incendo:cloud-minecraft-extras:2.0.0-beta.15")
    implementation("org.incendo:cloud-paper:2.0.0-beta.15")

    compileOnly(libs.placeholderapi)
    compileOnly(libs.luckperms)
    compileOnly(libs.mcmmo) { isTransitive = false }

    // Currency
    compileOnly(libs.vault)
    compileOnly(libs.rediseconomy)
    compileOnly(files("../libs/CoinsEngine-2.3.5.jar"))
    compileOnly(libs.playerpoints)

    // Eco Items
    compileOnly(libs.bundles.eco) { isTransitive = false }
    compileOnly(libs.eco.items)

    compileOnly(libs.zauctionhouse)
    compileOnly(files("../libs/AuctionHouse-1.20.4-3.7.1.jar"))
    compileOnly(files("../libs/AkarianAuctionHouse-1.3.1-b6.jar"))
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}
