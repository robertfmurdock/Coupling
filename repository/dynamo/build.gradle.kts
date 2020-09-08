
import com.zegreatrob.coupling.build.BuildConstants

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}



kotlin {
    targets {
        js {
            useCommonJs()
            nodejs {
                testTask {
                    useMocha {
                        timeout = "10s"
                    }
                }
            }
        }
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                api(project(":model"))
                api(project(":repository"))
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
                implementation("com.soywiz.korlibs.klock:klock:1.12.0")
                implementation("com.benasher44:uuid:0.2.2")
                implementation("io.github.microutils:kotlin-logging-common:1.8.3")
            }
        }
        getByName("commonTest") {
            dependencies {
                api(project(":repository:validation"))
                api(project(":stub-model"))
                implementation("com.zegreatrob.testmints:standard:3.1.8")
                implementation("com.zegreatrob.testmints:minassert:3.1.8")
                implementation("com.zegreatrob.testmints:async:3.1.8")
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                implementation("org.jetbrains.kotlin:kotlin-test-js")
            }
        }

        val jsMain by getting {
            dependencies {
                api(project(":logging"))
                implementation(npm("aws-sdk", "2.615.0"))
            }
        }
    }
}

tasks {

}
