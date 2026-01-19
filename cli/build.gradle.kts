@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.zegreatrob.tools.tagger.ReleaseVersion
import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
    kotlin("plugin.serialization")
    alias(libs.plugins.com.apollographql.apollo)
    distribution
}

repositories {
    google()
}

kotlin {
    jvm {
        binaries {
            executable {
                mainClass.set("com.zegreatrob.coupling.cli.MainKt")
            }
        }
    }
    js {
        nodejs {
            useEsModules()
            compilerOptions { target = "es2015" }
            binaries.executable()
            testTask {
                environment("COUPLING_VERSION", project.version.toString())
                environment("SKIP_AUTH", "true")
            }
        }
        compilations {
            "main" {
                packageJson {
                    name = "@continuous-excellence/coupling-cli"
                    customField("package-name", "coupling-cli")
                    customField("author", "rob@continuousexcellence.io")
                    customField("license", "MIT")
                    customField("keywords", arrayOf("git", "contribution", "pair", "agile", "coaching", "statistics"))
                    customField("bin", mapOf("coupling" to "kotlin/bin/coupling"))
                    customField("homepage", "https://github.com/robertfmurdock/Coupling")
                    customField("repository", "git+https://github.com/robertfmurdock/Coupling.git")
                }
            }
        }
    }
}

apollo {
    service("service") {
        packageName.set("com.zegreatrob.coupling.cli.gql")
        dependsOn(project(":sdk"))
    }
}

dependencies {
    commonMainImplementation(project(":libraries:auth0-management"))
    commonMainImplementation(project(":libraries:action"))
    commonMainImplementation(project(":libraries:model"))
    commonMainImplementation(project(":sdk"))
    commonMainImplementation("com.github.ajalt.clikt:clikt")
    commonMainImplementation("com.zegreatrob.tools:digger-json")
    commonMainImplementation("io.ktor:ktor-client-content-negotiation")
    commonMainImplementation("io.ktor:ktor-client-core")
    commonMainImplementation("io.ktor:ktor-client-logging")
    commonMainImplementation("io.ktor:ktor-serialization-kotlinx-json")
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-node")
    jsMainImplementation(npmConstrained("open"))
    jsMainImplementation(npmConstrained("jwt-decode"))
    jsMainImplementation(npmConstrained("@napi-rs/keyring"))
    "jvmMainImplementation"("com.auth0:java-jwt")
    "jvmMainImplementation"("org.slf4j:slf4j-api")
    "jvmMainImplementation"("org.slf4j:slf4j-simple")
    "jvmMainImplementation"("eu.anifantakis:ksafe")

    commonTestImplementation(kotlin("test"))
    commonTestImplementation(project(":libraries:test-action"))
    commonTestImplementation(project(":libraries:stub-model"))
    commonTestImplementation("com.zegreatrob.testmints:standard")
    commonTestImplementation("com.zegreatrob.testmints:async")
}

version = rootProject.version

tasks {
    named<Test>("jvmTest") {
        environment("COUPLING_VERSION", project.version.toString())
        environment("SKIP_AUTH" to "true")
    }
    withType<CreateStartScripts> {
        applicationName = "coupling"
    }
    distTar {
        compression = Compression.GZIP
        archiveFileName.set("coupling-cli.tgz")
    }

    val mainNpmProjectDir = kotlin.js().compilations.getByName("main").npmProject.dir

    val copyWebpackConfig by registering(Copy::class) {
        from(project.projectDir.resolve("webpack.config.js"))
        into(mainNpmProjectDir)
    }
    rootProject.tasks.named("rootPackageJson").configure {
        mustRunAfter(copyWebpackConfig)
    }

    val jsProcessResources by named<ProcessResources>("jsProcessResources") {
        dependsOn("dependencyResources")
    }

    register("dependencyResources", Copy::class) {
        dependsOn(":sdk:jsProcessResources")
        into(jsProcessResources.destinationDir)
        from("$rootDir/sdk/build/processedResources/js/main")
    }

    distTar {
        duplicatesStrategy = DuplicatesStrategy.WARN
    }

    val jsCliTar by registering(Tar::class) {
        dependsOn(
            "jsPackageJson",
            ":kotlinNpmInstall",
            "compileKotlinJs",
            jsProcessResources,
            "compileProductionExecutableKotlinJs",
            "jsProductionExecutableCompileSync",
        )
        from(mainNpmProjectDir)
        compression = Compression.GZIP
        archiveFileName.set("coupling-cli-js.tgz")
    }
    register("jsLink", Exec::class) {
        dependsOn(jsCliTar)
        workingDir(mainNpmProjectDir)
        commandLine("npm", "link")
    }
    val jsPublish by registering(Exec::class) {
        dependsOn(jsCliTar)
        enabled = !isSnapshot()
        mustRunAfter(check, ":release", ":deploy:prod:deploy")
        workingDir(mainNpmProjectDir)
        commandLine("npm", "publish")
    }
    register("publish") {
        dependsOn(jsPublish)
    }

    val uploadToS3 by registering(Exec::class) {
        dependsOn(jsCliTar, distTar)
        if (("${rootProject.version}").run { contains("SNAPSHOT") || isBlank() }) {
            enabled = false
        }
        val absolutePath = jsCliTar.get().destinationDirectory.get().asFile.absolutePath
        commandLine =
            "aws s3 sync $absolutePath s3://assets.zegreatrob.com/coupling-cli/${rootProject.version}".split(" ")
    }
    rootProject
        .tasks
        .withType(ReleaseVersion::class.java)
        .named("release")
        .configure {
            finalizedBy(uploadToS3)
        }
    val generatedDirectory = project.layout.buildDirectory.dir("generated-sources/templates/kotlin/main")
    val copyTemplates by registering(Copy::class) {
        inputs.property("version", rootProject.version)
        filteringCharset = "UTF-8"
        from(project.projectDir.resolve("src/commonMain/templates")) {
            filter<ReplaceTokens>("tokens" to mapOf("COUPLING_VERSION" to rootProject.version))
        }
        into(generatedDirectory)
    }
    withType<KotlinCompile> {
        dependsOn(copyTemplates)
    }
    kotlin.sourceSets {
        commonMain { kotlin.srcDir(copyTemplates) }
    }

}

fun Project.isSnapshot() = version.toString().contains("SNAPSHOT") || version == "unspecified"
