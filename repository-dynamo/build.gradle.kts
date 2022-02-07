
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
                api("com.zegreatrob.coupling.libraries:model")
                api("com.zegreatrob.coupling.libraries:repository-core")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
                implementation("com.soywiz.korlibs.klock:klock:2.4.13")
                implementation("com.benasher44:uuid:0.4.0")
                implementation("io.github.microutils:kotlin-logging:2.1.21")
            }
        }
        getByName("commonTest") {
            dependencies {
                api("com.zegreatrob.coupling.libraries:repository-validation")
                api("com.zegreatrob.coupling.libraries:stub-model")
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
                api("com.zegreatrob.coupling.libraries:logging")
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
