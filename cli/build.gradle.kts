
import com.zegreatrob.tools.tagger.ReleaseVersion
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrLink
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject

plugins {
    application
    id("com.zegreatrob.coupling.plugins.mp")
    kotlin("plugin.serialization")
}

application {
    mainClass.set("com.zegreatrob.coupling.cli.MainKt")
}

kotlin {
    jvm { withJava() }
    js {
        nodejs {
            useCommonJs()
            binaries.executable()
        }
        compilations {
            "main" {
                packageJson {
                    customField("bin", mapOf("coupling" to "./kotlin/bin/coupling"))
                }
            }
        }
    }
}

dependencies {
    commonMainImplementation(project(":libraries:auth0-management"))
    commonMainImplementation(project(":libraries:action"))
    commonMainImplementation(project(":libraries:model"))
    commonMainImplementation(project(":sdk"))
    commonMainImplementation(libs.com.github.ajalt.clikt.clikt)
    commonMainImplementation("com.benasher44:uuid")
    commonMainImplementation("com.zegreatrob.tools:digger-json")
    commonMainImplementation("io.ktor:ktor-client-content-negotiation")
    commonMainImplementation("io.ktor:ktor-client-core")
    commonMainImplementation("io.ktor:ktor-client-logging")
    commonMainImplementation("io.ktor:ktor-serialization-kotlinx-json")
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    "jsMainImplementation"("org.jetbrains.kotlin-wrappers:kotlin-node")
    "jsMainImplementation"("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    "jvmMainImplementation"("org.slf4j:slf4j-api")
    "jvmMainImplementation"("org.slf4j:slf4j-simple")

    commonTestImplementation(kotlin("test"))
    commonTestImplementation(project(":libraries:test-action"))
    commonTestImplementation(project(":libraries:stub-model"))
    commonTestImplementation("com.zegreatrob.testmints:standard")
    commonTestImplementation("com.zegreatrob.testmints:async")
}

version = rootProject.version

tasks {
    withType<CreateStartScripts> {
        applicationName = "coupling"
    }
    distTar {
        compression = Compression.GZIP
        archiveFileName.set("coupling-cli.tgz")
    }
    val compileProductionExecutableKotlinJs by named<KotlinJsIrLink>("compileProductionExecutableKotlinJs")

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

    val dependencyResources by registering(Copy::class) {
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
    val jsLink by registering(Exec::class) {
        dependsOn(jsCliTar)
        workingDir(mainNpmProjectDir)
        commandLine("npm", "link")
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

}
