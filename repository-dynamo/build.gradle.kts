
import com.zegreatrob.coupling.build.BuildConstants

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.zegreatrob.coupling.plugins.versioning")
    id("com.zegreatrob.coupling.plugins.reports")
    id("com.zegreatrob.coupling.plugins.testLogging")
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
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
                implementation("com.soywiz.korlibs.klock:klock:2.4.8")
                implementation("com.benasher44:uuid:0.3.1")
                implementation("io.github.microutils:kotlin-logging:2.1.16")
            }
        }
        getByName("commonTest") {
            dependencies {
                api(project(":repository-validation"))
                api(project(":stub-model"))
                implementation("com.zegreatrob.testmints:standard:5.3.8")
                implementation("com.zegreatrob.testmints:minassert:5.3.8")
                implementation("com.zegreatrob.testmints:async:5.3.8")
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                implementation("org.jetbrains.kotlin:kotlin-test-js")
            }
        }

        val jsMain by getting {
            dependencies {
                api(project(":logging"))
                implementation(npm("@aws-sdk/client-dynamodb", "3.18.0"))
                implementation(npm("@aws-sdk/lib-dynamodb", "3.18.0"))
            }
        }
    }
}

tasks {

}
