@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.ProjectLocalConfigurations
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsCompilerAttribute
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
    alias(libs.plugins.com.zegreatrob.jsmints.plugins.wdiotest)
}

kotlin {
    js {
        nodejs { testTask { enabled = false } }
    }
}

kotlin {
    sourceSets {
        getByName("e2eTest") {
            dependencies {
                implementation(project(":sdk"))
                implementation(project(":coupling-libraries:test-logging"))
                implementation(kotlin("test-js"))
                implementation("io.github.microutils:kotlin-logging")
                implementation("com.zegreatrob.testmints:standard")
                implementation("com.zegreatrob.testmints:minassert")
                implementation("com.zegreatrob.testmints:async")
                implementation("com.zegreatrob.jsmints:wdio")
                implementation("com.zegreatrob.jsmints:wdio-testing-library")


                implementation(npmConstrained("@log4js-node/log4js-api"))
                implementation(npmConstrained("@wdio/reporter"))
                implementation(npmConstrained("@wdio/dot-reporter"))
                implementation(npmConstrained("@wdio/jasmine-framework"))
                implementation(npmConstrained("@wdio/junit-reporter"))
                implementation(npmConstrained("chromedriver"))
                implementation(npmConstrained("fs-extra"))
                implementation(npmConstrained("wdio-html-nice-reporter"))
                implementation(npmConstrained("webpack"))
                implementation(npmConstrained("webpack-node-externals"))
                implementation(npmConstrained("wdio-chromedriver-service"))
                implementation(npmConstrained("jwt-decode"))
            }
        }
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
    implementation(kotlin("stdlib"))
    implementation("com.soywiz.korlibs.klock:klock")
    implementation("io.github.microutils:kotlin-logging")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-js")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-node")
    implementation("com.benasher44:uuid")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("com.zegreatrob.jsmints:wdio")
}

tasks {
    val e2eTestProcessResources = named<ProcessResources>("e2eTestProcessResources") {
        dependsOn("dependencyResources")
    }

    val dependencyResources by registering(Copy::class) {
        dependsOn(":client:processResources")
        duplicatesStrategy = DuplicatesStrategy.WARN
        into(e2eTestProcessResources.map { it.destinationDir })
        from("$rootDir/client/build/processedResources/js/main")
    }

    named("e2eRun", com.zegreatrob.jsmints.plugins.NodeExec::class) {
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
            "BASEURL" to "https://localhost/local/",
            "NODE_TLS_REJECT_UNAUTHORIZED" to 0,
            "CLIENT_BASENAME" to "local",
        )
    }

}

rootProject.extensions.findByType(NodeJsRootExtension::class.java).let {
    it?.nodeVersion = "19.6.0"
}
