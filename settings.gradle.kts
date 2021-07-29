@file:Suppress("UnstableApiUsage", "EXPERIMENTAL_IS_NOT_ENABLED")
@file:OptIn(okio.ExperimentalFileSystem::class)

import okio.Path.Companion.toOkioPath

gradle.logSomeEventsToFile(rootProject.projectDir.toOkioPath() / "my.gradle.log")

pluginManagement {
    includeBuild("../deps.kt")
}

plugins {
    id("pl.mareklangiewicz.deps.settings")
}

rootProject.name = "upue"

include(":upue")
