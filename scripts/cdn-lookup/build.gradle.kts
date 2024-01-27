import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrLink
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
    kotlin("plugin.serialization")
}

kotlin {
    js {
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
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    jsMainImplementation(npmConstrained("resolve-pkg"))
    jsMainImplementation(npmConstrained("read-pkg-up"))

    jsTestImplementation(kotlin("test"))
    jsTestImplementation(npmConstrained("react"))
    jsTestImplementation("com.zegreatrob.testmints:standard")
    jsTestImplementation("com.zegreatrob.testmints:async")
    jsTestImplementation("com.zegreatrob.testmints:minassert")
}

val cdnLookupConfiguration: Configuration by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    attributes {
        attribute(Attribute.of("com.zegreatrob.executable", String::class.java), "cdnLookupConfiguration")
    }
}

val outputFile: String? by project

tasks {
    named("jsNodeRun", NodeJsExec::class) {
        this.args("react")
        outputFile?.let {
            standardOutput = file("${System.getProperty("user.dir")}/$it").outputStream()
        }
    }
}

artifacts {
    val task = tasks.named("compileProductionExecutableKotlinJs", KotlinJsIrLink::class)
    add(cdnLookupConfiguration.name, task.map {
        it.destinationDirectory.file(it.compilerOptions.moduleName.map { moduleName -> "$moduleName.js" })
    }) {
        builtBy(task)
    }
}
