import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*

plugins {
  plug(plugs.NexusPublish)
  plug(plugs.KotlinMulti) apply false
}

defaultBuildTemplateForRootProject(
  myLibDetails(
    name = "UPue",
    description = "Micro Multiplatform Reactive Library.",
    githubUrl = "https://github.com/mareklangiewicz/UPue",
    version = Ver(0, 0, 21),
    // https://s01.oss.sonatype.org/content/repositories/releases/pl/mareklangiewicz/upue/
    // https://github.com/mareklangiewicz/UPue/releases
    settings = LibSettings(
      withNativeLinux64 = true,
      compose = null,
      withSonatypeOssPublishing = true,
    ),
  ),
)

// region [[Root Build Template]]

fun Project.defaultBuildTemplateForRootProject(details: LibDetails? = null) {
  details?.let {
    rootExtLibDetails = it
    defaultGroupAndVerAndDescription(it)
  }
}

// endregion [[Root Build Template]]
