@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.ProjectLocalConfigurations
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsCompilerAttribute

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
    id("com.zegreatrob.jsmints.plugins.wdiotest")
}

kotlin {
    js {
        nodejs { testTask { enabled = false } }
    }
}

val appConfiguration: Configuration by configurations.creating {
    attributes {
        attribute(KotlinJsCompilerAttribute.jsCompilerAttribute, KotlinJsCompilerAttribute.ir)
        attribute(ProjectLocalConfigurations.ATTRIBUTE, ProjectLocalConfigurations.PUBLIC_VALUE)
        attribute(KotlinPlatformType.attribute, KotlinPlatformType.js)
    }
}

val testLoggingLib: Configuration by configurations.creating
val clientConfiguration: Configuration by configurations.creating

dependencies {
    clientConfiguration(project(mapOf("path" to ":client", "configuration" to "clientConfiguration")))
    appConfiguration(project(mapOf("path" to ":server", "configuration" to "appConfiguration")))
    e2eTestImplementation(project(":sdk"))
    e2eTestImplementation(project(":coupling-libraries:test-logging"))
    e2eTestImplementation(kotlin("test-js"))
    e2eTestImplementation("io.github.microutils:kotlin-logging")
    e2eTestImplementation("com.zegreatrob.testmints:standard")
    e2eTestImplementation("com.zegreatrob.testmints:minassert")
    e2eTestImplementation("com.zegreatrob.testmints:async")
    e2eTestImplementation("com.zegreatrob.jsmints:wdio")
    e2eTestImplementation("com.zegreatrob.jsmints:wdio-testing-library")
    e2eTestImplementation(npmConstrained("fs-extra"))
    e2eTestImplementation(npmConstrained("jwt-decode"))
}

wdioTest {
    baseUrl.set("https://localhost/local/")
    htmlReporter.set(true)
    allureReporter.set(true)
    allureReportHint.set("")
}

tasks {
    val e2eTestProcessResources = named<ProcessResources>("e2eTestProcessResources") {
        dependsOn("dependencyResources")
    }

    val dependencyResources by registering(Copy::class) {
        dependsOn(":sdk:jsProcessResources")
        into(e2eTestProcessResources.map { it.destinationDir })
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
            "CLIENT_BASENAME" to "local",
        )
    }

}
