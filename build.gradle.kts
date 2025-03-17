import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.support.uppercaseFirstChar

plugins {
    java
    `maven-publish`
    id("org.ajoberstar.grgit") version "5.3.0"
    id("com.gradleup.shadow") version "8.3.0"
}

var currentBranch: String = grgit.branch.current().name
val devMode = currentBranch != "master" && currentBranch != "HEAD"
if (devMode) {
    println("Starting in development mode")
}

allprojects {
    group = "info.preva1l.fadah"
    version = "2.9.0"

    repositories {
        mavenCentral()
        maven(url = "https://repo.papermc.io/repository/maven-public/")
        if (devMode) configureFinallyADecentRepository(dev = true)
        configureFinallyADecentRepository()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "com.gradleup.shadow")

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.36")
        annotationProcessor("org.projectlombok:lombok:1.18.36")

        compileOnly("org.jetbrains:annotations:26.0.2")
        annotationProcessor("org.jetbrains:annotations:26.0.2")

        testImplementation("org.junit.jupiter:junit-jupiter-api:5.12.1")
        testImplementation("org.junit.jupiter:junit-jupiter-params:5.12.0")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.12.0")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.0")

        testCompileOnly("org.jetbrains:annotations:26.0.2")
        testCompileOnly("org.projectlombok:lombok:1.18.36")
        testAnnotationProcessor("org.projectlombok:lombok:1.18.36")
    }

    tasks.withType<ShadowJar> {
        destinationDirectory.set(file("$rootDir/target"))
        archiveFileName.set("${rootProject.name}-${project.name.uppercaseFirstChar()}-$version.jar")
        exclude(
            "META-INF/maven"
        )
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
        options.isFork = true
        options.encoding = "UTF-8"
    }

    tasks.register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    tasks.withType<Javadoc> {
        (options as StandardJavadocDocletOptions).tags(
            "apiNote:a:API Note:",
            "implSpec:a:Implementation Requirements:",
            "implNote:a:Implementation Note:"
        )
    }

    tasks.register<Jar>("javadocJar") {
        dependsOn("javadoc")
        archiveClassifier.set("javadoc")
        from(tasks.named<Javadoc>("javadoc").get().destinationDir)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<ProcessResources> {
        val tokenMap = variables()
        filesMatching(listOf("**/*.json", "**/plugin.yml")) {
            expand(tokenMap)
        }
    }

    publishing {
        repositories.configureFinallyADecentRepository(dev = devMode)

        publications {
            register(
                name = "mavenJava",
                type = MavenPublication::class,
                configurationAction = {
                    from(components["java"])

                    artifact(tasks.named("sourcesJar"))
                    artifact(tasks.named("javadocJar"))
                }
            )
        }
    }

    tasks.getByName("build")
        .dependsOn(
            "shadowJar"
        )
}

fun RepositoryHandler.configureFinallyADecentRepository(dev: Boolean = false) {
    val user: String? = properties["fad_username"]?.toString() ?: System.getenv("fad_username")
    val pass: String? = properties["fad_password"]?.toString() ?: System.getenv("fad_password")

    if (user != null && pass != null) {
        maven("https://repo.preva1l.info/${if (dev) "development" else "releases"}/") {
            name = "FinallyADecent"
            credentials {
                username = user
                password = pass
            }
        }
        return
    }

    maven("https://repo.preva1l.info/${if (dev) "development" else "releases"}/") {
        name = "FinallyADecent"
    }
}

fun variables(): Map<String, String> =
    mapOf(
        "version" to rootProject.version.toString(),
        "configlibVersion" to rootProject.property("configlibVersion") as String,
        "rhinoVersion" to rootProject.property("rhinoVersion") as String,
        "acp2Version" to rootProject.property("acp2Version") as String,
        "redissonVersion" to rootProject.property("redissonVersion") as String,
        "hikariVersion" to rootProject.property("hikariVersion") as String,
        "sqliteVersion" to rootProject.property("sqliteVersion") as String,
        "mysqlVersion" to rootProject.property("mysqlVersion") as String,
        "mariadbVersion" to rootProject.property("mariadbVersion") as String,
        "mongoVersion" to rootProject.property("mongoVersion") as String,
        "influxdbVersion" to rootProject.property("influxdbVersion") as String,
        "minimessageVersion" to rootProject.property("minimessageVersion") as String,
        "mmPlatformVersion" to rootProject.property("mmPlatformVersion") as String,
    )

project.delete("$rootDir/target")
logger.lifecycle("Building Fadah $version")