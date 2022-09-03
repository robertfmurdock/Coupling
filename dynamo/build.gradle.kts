import java.time.Duration

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
    id("com.avast.gradle.docker-compose")
}

kotlin {
    js {
        nodejs {
            testTask {
                useMocha {
                    timeout = "20s"
                }
            }
        }
    }
}

tasks {
    named("nodeTest") {
        dependsOn("composeUp")
    }
}

dockerCompose {
    tcpPortsToIgnoreWhenWaiting.set(listOf(5555))
    setProjectName("Coupling-root")
    startedServices.set(listOf("dynamo"))
    containerLogToDir.set(project.file("build/test-output/containers-logs"))
    waitForTcpPorts.set(false)
    waitAfterHealthyStateProbeFailure.set(Duration.ofMillis(100))
}

dependencies {
    api(project(":coupling-libraries:model"))
    api(project(":coupling-libraries:repository-core"))
    api(project(":coupling-libraries:logging"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    api("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    implementation("com.soywiz.korlibs.klock:klock")
    implementation("com.benasher44:uuid")
    implementation("io.github.microutils:kotlin-logging")

    testImplementation(project(":coupling-libraries:repository-validation"))
    testImplementation(project(":coupling-libraries:stub-model"))
    testImplementation("com.zegreatrob.testmints:standard")
    testImplementation("com.zegreatrob.testmints:minassert")
    testImplementation("com.zegreatrob.testmints:async")
    testImplementation("org.jetbrains.kotlin:kotlin-test-common")
    testImplementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
    testImplementation("org.jetbrains.kotlin:kotlin-test-js")
}
