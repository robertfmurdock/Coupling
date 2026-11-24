@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

// Kotlin/JS executable binary configuration still requires this opt-in.
import com.zegreatrob.tools.tagger.ReleaseVersion
import com.zegreatrob.coupling.plugins.deploy.S3SyncTask
import com.zegreatrob.coupling.plugins.js.NpmPackTask
import com.zegreatrob.coupling.plugins.js.VerifyNpmPackageTask
import com.zegreatrob.coupling.plugins.js.registerCliNpmPublication
import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrLink
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.RootPackageJsonTask
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinPackageJsonTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
    kotlin("plugin.serialization")
    alias(libs.plugins.apollo)
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
                environment("COUPLING_VERSION", project.version.toString().let { if (it == "unspecified") "0.0.0-unspecified" else it })
                environment("COUPLING_CLI_ACCESS_TOKEN", "fake")
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
        dependsOn(dependencies.project(":sdk"))
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
    named<KotlinJsIrLink>("compileProductionExecutableKotlinJs") {
        // Kotlin 2.4.21 DCE expands this interface via HashSet, producing unstable interface metadata order.
        compilerOptions.freeCompilerArgs.add("-Xir-keep=kotlinx.coroutines.channels.Channel")
        // Keeping Channel.iterator requires consistent names across its stdlib overrides.
        compilerOptions.freeCompilerArgs.add("-Xir-minimized-member-names=false")
    }
    withType<KotlinPackageJsonTask>().configureEach {
        inputs.property("cliVersion", project.version.toString())
    }
    named<Test>("jvmTest") {
        environment("COUPLING_VERSION", project.version.toString())
        environment("COUPLING_CLI_ACCESS_TOKEN" to "fake")
    }
    withType<CreateStartScripts> {
        applicationName = "coupling"
    }
    distTar {
        compression = Compression.GZIP
        archiveFileName.set("coupling-cli.tgz")
    }

    val mainNpmProjectDir = kotlin.js().compilations.getByName("main").npmProject.dir

    val copyWebpackConfig = register<Copy>( "copyWebpackConfig") {
        from(project.projectDir.resolve("webpack.config.js"))
        into(mainNpmProjectDir)
    }
    rootProject.tasks.withType<RootPackageJsonTask>().configureEach {
        mustRunAfter(copyWebpackConfig)
    }

    distTar {
        duplicatesStrategy = DuplicatesStrategy.WARN
    }

    val jsCliTar = register<Tar>("jsCliTar") {
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
    val npmPack = register<NpmPackTask>("npmPack") {
        dependsOn(jsCliTar)
        packageDirectory.set(mainNpmProjectDir)
        archiveFile.set(layout.buildDirectory.file("npm/coupling-cli.tgz"))
        packageVersion.set(project.version.toString().let { if (it == "unspecified") "0.0.0-unspecified" else it })
    }
    val verifyNpmPackage = register<VerifyNpmPackageTask>("verifyNpmPackage") {
        archiveFile.set(npmPack.flatMap { it.archiveFile })
    }
    check { dependsOn(verifyNpmPackage) }
    project.registerCliNpmPublication(npmPack, verifyNpmPackage, isSnapshot())

    val uploadToS3 = register<S3SyncTask>("uploadToS3") {
        group = "deployment"
        description = "Synchronizes the CLI distribution to S3."
        dependsOn(jsCliTar, distTar)
        if (("${rootProject.version}").run { contains("SNAPSHOT") || isBlank() }) {
            enabled = false
        }
        sourceDirectory.set(jsCliTar.flatMap { it.destinationDirectory })
        destination.set("s3://assets.zegreatrob.com/coupling-cli/${rootProject.version}")
    }
    rootProject
        .tasks
        .withType(ReleaseVersion::class.java)
        .named("release")
        .configure {
            finalizedBy(uploadToS3)
        }
    val generatedDirectory = project.layout.buildDirectory.dir("generated-sources/templates/kotlin/main")
    val copyTemplates = register<Copy>("copyTemplates") {
        inputs.property("version", rootProject.version)
        filteringCharset = "UTF-8"
        from(project.projectDir.resolve("src/jvmMain/templates")) {
            filter<ReplaceTokens>("tokens" to mapOf("COUPLING_VERSION" to rootProject.version))
        }
        into(generatedDirectory)
    }
    withType<KotlinCompile> {
        dependsOn(copyTemplates)
    }
    kotlin.sourceSets {
        named("jvmMain") { kotlin.srcDir(copyTemplates) }
    }

}

fun Project.isSnapshot() = version.toString().contains("SNAPSHOT") || version == "unspecified"
