@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
    id("com.zegreatrob.jsmints.plugins.wdiotest")
    alias(libs.plugins.com.apollographql.apollo)
}

kotlin {
    js {
        useCommonJs()
        nodejs { testTask { enabled = false } }
    }
}

val appConfiguration: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Attribute.of("com.zegreatrob.executable", String::class.java), "server")
    }
}

val testLoggingLib: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Attribute.of("com.zegreatrob.executable", String::class.java), "test-logging")
    }
}

val clientConfiguration: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Attribute.of("com.zegreatrob.executable", String::class.java), "client")
    }
}

apollo {
    service("service") {
        packageName.set("com.zegreatrob.coupling.e2e.gql")
        dependsOn(project(":sdk"))
        srcDir("src/jsE2eTest/graphql")
        generateSourcesDuringGradleSync.set(true)
        outputDirConnection {
            connectToKotlinSourceSet("jsE2eTest")
        }
    }
}

dependencies {
    clientConfiguration(project(":client"))
    appConfiguration(project(":server"))
    jsE2eTestImplementation(platform(project(":libraries:dependency-bom")))
    jsE2eTestImplementation(project(":sdk"))
    jsE2eTestImplementation(project(":libraries:test-logging"))
    jsE2eTestImplementation(project(":libraries:stub-model"))
    jsE2eTestImplementation(kotlin("test-js"))
    jsE2eTestImplementation("com.zegreatrob.jsmints:wdio")
    jsE2eTestImplementation("com.zegreatrob.jsmints:wdio-testing-library")
    jsE2eTestImplementation("com.zegreatrob.testmints:async")
    jsE2eTestImplementation("com.zegreatrob.testmints:minassert")
    jsE2eTestImplementation("com.zegreatrob.testmints:standard")
    jsE2eTestImplementation("io.github.oshai:kotlin-logging")
    jsE2eTestImplementation("io.ktor:ktor-client-content-negotiation")
    jsE2eTestImplementation("io.ktor:ktor-client-core")
    jsE2eTestImplementation("io.ktor:ktor-client-logging")
    jsE2eTestImplementation("io.ktor:ktor-serialization-kotlinx-json")
    jsE2eTestImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    jsE2eTestImplementation(npmConstrained("fs-extra"))
    jsE2eTestImplementation(npmConstrained("jwt-decode"))
}

wdioTest {
    baseUrl.set("https://localhost/")
    htmlReporter.set(true)
    useHeadless.set(true)
    chromeBinary.set(System.getenv("WDIO_CHROME_BINARY"))
}

tasks {

    jsE2eTestProcessResources {
        dependsOn("dependencyResources")
    }

    val dependencyResources by registering(Copy::class) {
        dependsOn(":sdk:jsProcessResources")
        into(jsE2eTestProcessResources.map { it.destinationDir })
        from("$rootDir/sdk/build/processedResources/js/main")
    }
    e2eRun {
        dependsOn(
            dependencyResources,
            appConfiguration,
            clientConfiguration,
            testLoggingLib,
            ":composeUp"
        )
        inputs.files(
            appConfiguration,
            clientConfiguration,
            testLoggingLib
        )
        environment(
            "NODE_TLS_REJECT_UNAUTHORIZED" to 0,
        )
    }

}
