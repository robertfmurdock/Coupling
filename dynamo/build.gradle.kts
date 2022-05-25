plugins {
    id("com.zegreatrob.coupling.plugins.mp")
    id("com.avast.gradle.docker-compose")
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
                api(project(":coupling-libraries:model"))
                api(project(":coupling-libraries:repository-core"))
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                api("org.jetbrains.kotlin-wrappers:kotlin-extensions")
                implementation("com.soywiz.korlibs.klock:klock:2.7.0")
                implementation("com.benasher44:uuid:0.4.0")
                implementation("io.github.microutils:kotlin-logging:2.1.23")
            }
        }
        getByName("commonTest") {
            dependencies {
                api(project(":coupling-libraries:repository-validation"))
                api(project(":coupling-libraries:stub-model"))
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
//                api(project(":coupling-libraries:logging"))
//                implementation(npm("@aws-sdk/client-dynamodb", "3.18.0"))
//                implementation(npm("@aws-sdk/lib-dynamodb", "3.18.0"))
            }
        }
    }
}

tasks {
    named("jsNodeTest") {
        dependsOn("composeUp")
    }
}

dockerCompose {
    tcpPortsToIgnoreWhenWaiting.set(listOf(5555))
    setProjectName("Coupling-root")
    startedServices.set(listOf("dynamo"))
    containerLogToDir.set(project.file("build/test-output/containers-logs"))
}
