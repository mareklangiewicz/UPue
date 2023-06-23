@file:Suppress("UnstableApiUsage", "EXPERIMENTAL_IS_NOT_ENABLED")

import okio.Path.Companion.toOkioPath
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.evts.*

//gradle.logSomeEventsToFile(rootProject.projectDir.toOkioPath() / "my.gradle.log")

pluginManagement {
//    includeBuild("../DepsKt")
    repositories {
        google()
        gradlePluginPortal()
    }
}

plugins {
    id("pl.mareklangiewicz.deps.settings") version "0.2.41"
}

rootProject.name = "UPue"

include(":upue")
include(":upue-test")

//includeAndSubstituteBuild("../USpek", deps.uspekx, ":uspekx")