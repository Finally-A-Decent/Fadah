tasks.register("clean") {
    doFirst {
        project.delete("$rootDir/target")
    }
}

logger.lifecycle("Building Fadah $version")