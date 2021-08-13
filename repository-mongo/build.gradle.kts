
import com.zegreatrob.coupling.build.BuildConstants

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    targets {
        js {
            nodejs {
                testTask {
                    if(System.getenv("SKIP_MONGO_TESTS") == "true") {
                        enabled = false
                    }
                    useMocha {
                        timeout = "10s"
                    }
                }
            }
            useCommonJs()
        }
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                api(project(":model"))
                api(project(":repository-core"))
                implementation("com.benasher44:uuid:0.3.0")
                implementation("com.soywiz.korlibs.klock:klock:2.1.0")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
            }
        }
        getByName("commonTest") {
            dependencies {
                api(project(":repository-validation"))
                api(project(":stub-model"))
                implementation("com.zegreatrob.testmints:standard:4.1.11")
                implementation("com.zegreatrob.testmints:minassert:4.1.11")
                implementation("com.zegreatrob.testmints:async:4.1.11")
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                implementation("org.jetbrains.kotlin:kotlin-test-js")
            }
        }

        val jsMain by getting {
            dependencies {
            }
        }
        val jsTest by getting {
            dependencies {
                api(project(":logging"))
                implementation(npm("monk", "7.1.1"))
                implementation(npm("mongodb", "3.5.0"))
            }
        }
    }
}

tasks {
}
