@file:Suppress("UnstableApiUsage")

import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.extLib

rootProject.name = "UPue"

// gradle.logSomeEventsToFile(rootProject.projectDir.toOkioPath() / "my.gradle.log")


// Careful with auto publishing fails/stack traces
val buildScanPublishingAllowed =
  System.getenv("GITHUB_ACTIONS") == "true"
  // true
  // false

// region [[My Settings Stuff <~~]]
// ~~>".*/Deps\.kt"~~>"../DepsKt"<~~
// endregion [[My Settings Stuff <~~]]
// region [[My Settings Stuff]]

pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }

  val depsDir = File(rootDir, "../DepsKt").normalize()
  val depsInclude =
    // depsDir.exists()
    false
  if (depsInclude) {
    logger.warn("Including local build $depsDir")
    includeBuild(depsDir)
  }
}

plugins {
  id("pl.mareklangiewicz.deps.settings") version "0.4.62" // https://plugins.gradle.org/search?term=mareklangiewicz
  id("com.gradle.develocity") version "4.5.1" // https://docs.gradle.com/develocity/gradle-plugin/
}

develocity {
  buildScan {
    termsOfUseUrl = "https://gradle.com/terms-of-service"
    termsOfUseAgree = "yes"
    publishing.onlyIf { buildScanPublishingAllowed && it.buildResult.failures.isNotEmpty() }
  }
}

// endregion [[My Settings Stuff]]

val enableJs = true
val enableNative = true

gradle.extLib = lib(
  info = myLibInfo(
    name = "UPue",
    description = "Micro Multiplatform Reactive Library.",
    githubUrl = "https://github.com/mareklangiewicz/UPue",
    version = Ver(0, 0, 24),
    // https://central.sonatype.com/artifact/pl.mareklangiewicz/upue/versions
    // https://github.com/mareklangiewicz/UPue/releases
  ),
  flags = LibFlags(
    // withJs is stated here on PURPOSE. The old LibSettings defaulted it to true and this repo
    // relied on that silently; LibFlags defaults it to false, so leaving it out would drop the
    // jsMain/jsTest source sets and still build green.
    withJs = enableJs,
    withLinuxX64 = enableNative,
    withCentralPublish = true,
  ),
  withCompose = false, // was: compose = null
  // andro is absent by default
)

include(":upue")
include(":upue-test")
