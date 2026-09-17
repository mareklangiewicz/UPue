
// region [[Basic MPP Lib Build Imports and Plugs]]

import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import com.vanniktech.maven.publish.*
import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.templatefun.*

plugins {
  id("pl.mareklangiewicz.templatefun")
  plugAll(plugs.KotlinMulti, plugs.VannikPublish)
}

// endregion [[Basic MPP Lib Build Imports and Plugs]]

// TODO_later: probably deprecate or rewrite into nicer multiplatform dsl?
// upue-test is old jvm / google-truth based assertion DSL used only in upue
// I'll probably deprecate it, let's stop publishing it - upue should be micro.
// Was: rootExtLibDetails.copy(settings = settings.copy(withCentralPublish = false)).
defaultBuildTemplateForBasicMppLib(
  lib = gradle.extLib.run { copy(flags = flags.copy(withCentralPublish = false)) },
)

kotlin {
  sourceSets {
    val jvmMain by getting {
      dependencies {
        api(Com.Google.Truth.truth)
      }
    }
  }
}
