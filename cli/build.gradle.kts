import com.zegreatrob.coupling.plugins.NodeExec
import com.zegreatrob.coupling.plugins.setup
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
    js { nodejs { binaries.executable() } }
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
    "jvmMainImplementation"("org.slf4j:slf4j-api")
    "jvmMainImplementation"("org.slf4j:slf4j-simple")
}

version = rootProject.version

tasks {
    distTar {
        compression = Compression.GZIP
        archiveFileName.set("coupling-cli.tgz")
    }
    val uploadToS3 by registering(Exec::class) {
        dependsOn(distTar)
        if (("${rootProject.version}").run { contains("SNAPSHOT") || isBlank() }) {
            enabled = false
        }
        val absolutePath = distTar.get().destinationDirectory.get().asFile.absolutePath
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

    val webpack by registering(NodeExec::class) {
        dependsOn(
            copyWebpackConfig,
            "jsPackageJson",
            ":kotlinNpmInstall",
            "compileKotlinJs",
            jsProcessResources,
            "compileProductionExecutableKotlinJs",
            "jsProductionExecutableCompileSync",
        )
        mustRunAfter(clean)
        inputs.file(file("webpack.config.js"))
        inputs.dir(jsProcessResources.destinationDir.path)
        inputs.file(compileProductionExecutableKotlinJs.let {
            it.destinationDirectory.file(it.compilerOptions.moduleName.map { name -> "$name.js" })
        })
        outputs.dir(mainNpmProjectDir.resolve("webpack-output"))
        val compilationName = "main"
        val compilation = kotlin.js().compilations.named(compilationName).get()

        inputs.file(compilation.npmProject.packageJsonFile)

        setup(project)
        nodeModulesDir = compilation?.npmProject?.nodeModulesDir
        npmProjectDir = compilation?.npmProject?.dir

        nodeCommand = "webpack"
        arguments = listOf("--config", mainNpmProjectDir.resolve("webpack.config.js").absolutePath)
    }
    assemble { dependsOn(webpack) }
}
