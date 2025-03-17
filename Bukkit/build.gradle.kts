import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val configlibVersion: String by project
val rhinoVersion: String by project
val acp2Version: String by project
val redissonVersion: String by project
val hikariVersion: String by project
val sqliteVersion: String by project
val mysqlVersion: String by project
val mariadbVersion: String by project
val mongoVersion: String by project
val influxdbVersion: String by project
val minimessageVersion: String by project
val mmPlatformVersion: String by project

repositories {
    maven(url = "https://repo.auxilor.io/repository/maven-public/")
    maven(url = "https://oss.sonatype.org/content/groups/public/")
    maven(url = "https://jitpack.io")
    maven(url = "https://repo.codemc.io/repository/maven-snapshots/")
    maven(url = "https://repo.clojars.org/")
    maven(url = "https://repo.william278.net/snapshots")
    maven(url = "https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven(url = "https://mvn-repo.arim.space/lesser-gpl3/")
}

dependencies {
    implementation(project(":API"))
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    compileOnly("de.exlll:configlib-yaml:$configlibVersion") // Configs

    implementation("com.github.puregero:multilib:1.2.4") // Folia & Shreddedpaper support

    implementation("net.william278:desertwell:2.0.4") // Update Checker & About Page

    // Cross Server Support
    compileOnly("org.redisson:redisson:$redissonVersion")
    compileOnly("org.apache.commons:commons-pool2:$acp2Version")

    compileOnly("me.clip:placeholderapi:2.11.6") // Placeholder support

    // Database
    compileOnly("com.zaxxer:HikariCP:$hikariVersion")
    compileOnly("org.xerial:sqlite-jdbc:$sqliteVersion")
    compileOnly("com.mysql:mysql-connector-j:$mysqlVersion")
    compileOnly("org.mariadb.jdbc:mariadb-java-client:$mariadbVersion")
    compileOnly("org.mongodb:mongodb-driver-sync:$mongoVersion")

    implementation("net.wesjd:anvilgui:1.10.4-SNAPSHOT") // Search Menu

    compileOnly("org.mozilla:rhino:$rhinoVersion")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7") // Economy Hook

    // Extra Hooks
    annotationProcessor("info.preva1l.hooker:Hooker:1.0.1")
    implementation("info.preva1l.hooker:Hooker:1.0.1")
    // Eco Items
    compileOnly("com.willfp:libreforge:4.58.1")
    compileOnly("com.willfp:eco:6.56.0")
    compileOnly("com.willfp:EcoItems:5.43.1")
    compileOnly("com.influxdb:influxdb-client-java:$influxdbVersion") // InfluxDB logging
    // Currency
    compileOnly("com.github.Emibergo02:RedisEconomy:4.3.9")
    compileOnly(files("../libs/CoinsEngine-2.3.5.jar"))

    // Migrators
    compileOnly("com.github.Maxlego08:zAuctionHouseV3-API:3.2.1.9") // zAuctionHouse
    compileOnly(files("../libs/AuctionHouse-1.20.4-3.7.1.jar")) // AuctionHouse
    compileOnly(files("../libs/AkarianAuctionHouse-1.3.1-b6.jar")) // AkarianAuctionHouse
}

tasks.withType<ShadowJar> {
    relocate("net.wesjd", "info.preva1l.fadah.libs")
    relocate("com.github.puregero.multilib", "info.preva1l.fadah.libs.multilib")
    relocate("info.preva1l.hooker", "info.preva1l.fadah.hooks.lib")
}
