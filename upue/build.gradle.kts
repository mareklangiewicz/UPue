plugins {
    kotlin("multiplatform") version Vers.kotlin
    id("maven-publish")
}

group = "pl.mareklangiewicz.upue"
version = "0.0.08"

repositories {
    mavenCentral()
    maven(Repos.jitpack)
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
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(Deps.junit4)
                implementation(Deps.googleTruth)
            }

        }
    }
}
