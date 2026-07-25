import com.zegreatrob.coupling.plugins.js.NodeExec
import com.zegreatrob.coupling.plugins.js.setup
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrLink
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
    kotlin("plugin.serialization")
}

kotlin {
    js {
        useEsModules()
        nodejs {
            binaries.executable()
            testTask { useMocha { timeout = "400s" } }
        }
        compilations {
            "main" {
                packageJson {
                    name = "@continuous-excellence/cdn-lookup"
                    customField("author", "rob@continuousexcellence.io")
                    customField("license", "MIT")
                    customField("keywords", arrayOf("cdn", "dependencies", "esm", "imports"))
                    customField("bin", mapOf("cdn-lookup" to "kotlin/bin/cdn-lookup"))
                    customField("homepage", "https://github.com/robertfmurdock/Coupling")
                    customField("repository", "git+https://github.com/robertfmurdock/Coupling.git")
                }
            }
        }
    }
}

dependencies {
    jsMainImplementation("io.ktor:ktor-client-content-negotiation")
    jsMainImplementation("io.ktor:ktor-client-core")
    jsMainImplementation("io.ktor:ktor-client-encoding")
    jsMainImplementation("io.ktor:ktor-client-logging")
    jsMainImplementation("io.ktor:ktor-serialization-kotlinx-json")
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-node")
    jsMainImplementation(npmConstrained("resolve-pkg"))
    jsMainImplementation(npmConstrained("read-pkg-up"))

    jsTestImplementation(kotlin("test"))
    jsTestImplementation("com.zegreatrob.testmints:standard")
    jsTestImplementation("com.zegreatrob.testmints:async")
    jsTestImplementation("com.zegreatrob.testmints:minassert")
    jsTestImplementation(npmConstrained("@auth0/auth0-react"))
    jsTestImplementation(npmConstrained("react-router"))
    jsTestImplementation("org.jetbrains.kotlin-wrappers:kotlin-react")
    jsTestImplementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
}

val cdnLookupConfiguration: Configuration = configurations.create("cdnLookupConfiguration") {
    isCanBeConsumed = true
    isCanBeResolved = false
    attributes {
        attribute(Attribute.of("com.zegreatrob.executable", String::class.java), "cdnLookupConfiguration")
    }
}

val outputFile: String? = project.findProperty("outputFile")?.toString()

tasks {
    val npmProjectDir = kotlin.js().compilations.named("main").map { it.npmProject.dir.get().asFile }
    val packDirectory = layout.buildDirectory.dir("distributions")
    val cdnLookupNpmPack = register("cdnLookupNpmPack", Exec::class) {
        dependsOn("jsPackageJson", ":kotlinNpmInstall", "jsProductionExecutableCompileSync")
        inputs.dir(npmProjectDir)
        outputs.file(packDirectory.map { it.file("continuous-excellence-cdn-lookup-0.0.0.tgz") })
        workingDir(npmProjectDir)
        doFirst { packDirectory.get().asFile.mkdirs() }
        commandLine("npm", "pack", "--pack-destination", packDirectory.get().asFile.absolutePath)
    }

    named("jsNodeProductionRun", NodeJsExec::class) {
        this.args("react")
        outputFile?.let {
            standardOutput = file("${System.getProperty("user.dir")}/$it").outputStream()
        }
    }

    register("packedExecutableSmokeTest", NodeExec::class) {
        setup(project)
        dependsOn(cdnLookupNpmPack)
        val archive = layout.buildDirectory.file("distributions/continuous-excellence-cdn-lookup-0.0.0.tgz")
        val fixture = layout.buildDirectory.dir("packed-executable-smoke-test")
        inputs.file(archive)
        inputs.file("src/packedTest/js/packed-executable-smoke-test.mjs")
        outputs.dir(fixture)
        arguments = listOf(
            layout.projectDirectory.file("src/packedTest/js/packed-executable-smoke-test.mjs").asFile.absolutePath,
            archive.get().asFile.absolutePath,
            fixture.get().asFile.absolutePath,
        )
    }
}

artifacts {
    val npmProjectDir = kotlin.js().compilations.named("main").map { it.npmProject.dir.get() }
    val moduleName = tasks.named("compileProductionExecutableKotlinJs", KotlinJsIrLink::class)
        .flatMap { it.compilerOptions.moduleName }
    val executable = npmProjectDir.zip(moduleName) { directory, name ->
        directory.file("kotlin/$name.mjs")
    }
    add(cdnLookupConfiguration.name, executable) {
        builtBy(tasks.named("jsProductionExecutableCompileSync"))
    }
}
