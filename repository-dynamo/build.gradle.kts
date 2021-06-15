
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
                        timeout = "20s"
                    }
                }
            }
        }
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                api(project(":model"))
                api(project(":repository-core"))
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
                implementation("com.soywiz.korlibs.klock:klock:2.1.0")
                implementation("com.benasher44:uuid:0.3.0")
                implementation("io.github.microutils:kotlin-logging:2.0.8")
            }
        }
        getByName("commonTest") {
            dependencies {
                api(project(":repository-validation"))
                api(project(":stub-model"))
                implementation("com.zegreatrob.testmints:standard:4.0.20")
                implementation("com.zegreatrob.testmints:minassert:4.0.20")
                implementation("com.zegreatrob.testmints:async:4.0.20")
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
