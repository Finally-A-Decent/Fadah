plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.preva1l.info/releases/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.20")
    implementation("com.gradleup.shadow:shadow-gradle-plugin:9.2.2")
    implementation("org.ajoberstar.grgit:org.ajoberstar.grgit.gradle.plugin:5.3.3")
    implementation("info.preva1l.trashcan:Trashcan-Tooling:1.0.2")
}