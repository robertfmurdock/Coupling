
import com.zegreatrob.coupling.build.BuildConstants

plugins {
    id("com.zegreatrob.coupling.plugins.mp")
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
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("com.soywiz.korlibs.klock:klock:2.4.12")
                implementation("com.benasher44:uuid:0.3.1")
                implementation("io.github.microutils:kotlin-logging:2.1.21")
            }
        }
        getByName("commonTest") {
            dependencies {
                api(project(":repository-validation"))
                api(project(":stub-model"))
                implementation("com.zegreatrob.testmints:standard")
                implementation("com.zegreatrob.testmints:minassert")
                implementation("com.zegreatrob.testmints:async")
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
    val jsNodeTest by getting
    jsNodeTest.dependsOn(":composeUp")
}
