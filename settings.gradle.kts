@file:Suppress("UnstableApiUsage", "EXPERIMENTAL_IS_NOT_ENABLED")

import okio.Path.Companion.toOkioPath
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.evts.*

gradle.logSomeEventsToFile(rootProject.projectDir.toOkioPath() / "my.gradle.log")

pluginManagement {
    includeBuild("../deps.kt")
}

plugins {
    id("pl.mareklangiewicz.deps.settings")
}

rootProject.name = "upue"

include(":upue")
include(":upue-test")

includeAndSubstituteBuild("../USpek", Deps.uspekx, ":uspekx")
