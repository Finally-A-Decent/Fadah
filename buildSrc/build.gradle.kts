plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.preva1l.info/releases/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.10")
    implementation("com.gradleup.shadow:shadow-gradle-plugin:8.3.6")
    implementation("org.ajoberstar.grgit:org.ajoberstar.grgit.gradle.plugin:5.3.0")
    implementation("info.preva1l.trashcan:Trashcan-Tooling:1.0.1")
}