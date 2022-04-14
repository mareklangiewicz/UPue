import pl.mareklangiewicz.defaults.*

plugins {
    kotlin("multiplatform")
    id("maven-publish")
    id("signing")
}

defaultGroupAndVerAndDescription(libs.UPue)

repositories {
    defaultRepos(withGoogle = false)
}

kotlin {
    jvm()
    js(IR) {
        browser {
            testTask {
                useKarma {
//                    useChrome()
                    useChromeHeadless()
                }
            }
        }
    }
//    linuxX64()

    @Suppress("UNUSED_VARIABLE")
    sourceSets {
        val commonTest by getting {
            dependencies {
                api(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                api(deps.junit4)
                api(deps.googleTruth)
            }
        }
    }
}

defaultPublishing(libs.UPue)

defaultSigning()