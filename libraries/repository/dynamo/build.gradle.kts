import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
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

dockerCompose {
    tcpPortsToIgnoreWhenWaiting.set(listOf(5555))
    setProjectName("Coupling-root")
    startedServices.set(listOf("dynamo"))
    containerLogToDir.set(project.file("build/test-output/containers-logs"))
    waitForTcpPorts.set(false)
    waitAfterHealthyStateProbeFailure.set(Duration.ofMillis(100))
}

dependencies {
    jsMainApi(project(":libraries:model"))
    jsMainApi(project(":libraries:repository:core"))
    jsMainApi(project(":libraries:logging"))
    jsMainApi("org.jetbrains.kotlin-wrappers:kotlin-js")
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    jsMainImplementation("io.github.oshai:kotlin-logging")
    jsMainImplementation(npmConstrained("@aws-sdk/client-dynamodb"))
    jsMainImplementation(npmConstrained("@aws-sdk/lib-dynamodb"))

    jsTestImplementation(project(":libraries:repository:validation"))
    jsTestImplementation(project(":libraries:stub-model"))
    jsTestImplementation("com.zegreatrob.testmints:async")
    jsTestImplementation("com.zegreatrob.testmints:minassert")
    jsTestImplementation("com.zegreatrob.testmints:standard")
    jsTestImplementation("org.jetbrains.kotlin:kotlin-test")
    jsTestImplementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
}

tasks {
    named("jsNodeTest", KotlinJsTest::class) {
        dependsOn("composeUp")
        environment("LOCAL_DYNAMO", "true")
    }
    named("composeUp") {
        mustRunAfter(":caddyComposeUp")
    }
}
