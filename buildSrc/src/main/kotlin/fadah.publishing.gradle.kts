plugins {
    `maven-publish`
}

publishing {
    repositories {
        maven {
            name = "FinallyADecent"
            url = uri(rootProject.layout.buildDirectory.dir("repo"))
        }
    }
}
