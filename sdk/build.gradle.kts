plugins {
    alias(libs.plugins.com.zegreatrob.tools.certifier)
    id("com.zegreatrob.coupling.plugins.jstools")
}

kotlin {
    js {
        nodejs { testTask { useMocha { timeout = "10s" } } }
    }
    jvm()
}

dependencies {
    "commonMainApi"(project(":libraries:action"))
    "commonMainApi"(project(":libraries:model"))
    "commonMainApi"(project(":libraries:json"))
    "commonMainImplementation"(project(":libraries:repository:core"))
    "commonMainImplementation"("io.ktor:ktor-client-content-negotiation")
    "commonMainImplementation"("io.ktor:ktor-client-core")
    "commonMainImplementation"("io.ktor:ktor-client-logging")
    "commonMainImplementation"("io.ktor:ktor-client-websockets")
    "commonMainImplementation"("io.ktor:ktor-serialization-kotlinx-json")
    "commonMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    "commonMainImplementation"("org.jetbrains.kotlinx:kotlinx-serialization-json")

    "commonTestImplementation"(project(":libraries:repository:validation"))
    "commonTestImplementation"(project(":libraries:stub-model"))
    "commonTestImplementation"(project(":libraries:test-logging"))
    "commonTestImplementation"("com.zegreatrob.testmints:async")
    "commonTestImplementation"("com.zegreatrob.testmints:minassert")
    "commonTestImplementation"("com.zegreatrob.testmints:standard")
    "commonTestImplementation"("io.github.oshai:kotlin-logging")
    "commonTestImplementation"("org.jetbrains.kotlin:kotlin-test")

    "jsMainImplementation"("org.jetbrains.kotlin-wrappers:kotlin-js")
    "jsMainImplementation"(npmConstrained("ws"))
    "jsTestImplementation"(project(":server:slack"))
    "jvmTestImplementation"("io.ktor:ktor-client-java")
}

tasks {
    val jsNodeTest by getting {
        dependsOn(":composeUp")
        outputs.cacheIf { true }
    }
    installCert {
        dependsOn(":caddyComposeUp")
        jdkSelector = "20"
        certificatePath = "${System.getenv("HOME")}/caddy_data/caddy/pki/authorities/local/root.crt"
    }
    "jvmTest" {
        mustRunAfter(jsNodeTest)
        dependsOn(":composeUp")
        dependsOn(installCert)
    }
}
