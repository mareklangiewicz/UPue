plugins {
    kotlin("multiplatform") version Vers.kotlin
    id("maven-publish")
}

defaultGroupAndVer(Deps.upue)

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
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                api(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                api(Deps.junit4)
                api(Deps.googleTruth)
            }
        }
    }
}
