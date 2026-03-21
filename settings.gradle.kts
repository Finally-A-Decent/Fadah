rootProject.name = "Fadah"
include("Bukkit", "API")

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io")
        maven("https://repo.preva1l.info/releases/")
    }
}
