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
    named("jsNodeProductionRun", NodeJsExec::class) {
        this.args("react")
        outputFile?.let {
            standardOutput = file("${System.getProperty("user.dir")}/$it").outputStream()
        }
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
