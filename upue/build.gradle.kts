import pl.mareklangiewicz.defaults.*

plugins {
    kotlin("multiplatform")
    id("maven-publish")
    id("signing")
}

repositories { defaultRepos(withGoogle = false) }

defaultGroupAndVerAndDescription(libs.UPue)

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

    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(project(":upue-test"))
                implementation(deps.uspekx)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(deps.junit5engine)
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

defaultPublishing(libs.UPue)

defaultSigning()