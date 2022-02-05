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

    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(project(":upue-test"))
                implementation(Deps.uspekx)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(Deps.junit5engine)
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
