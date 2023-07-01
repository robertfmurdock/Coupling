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
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-client-logging")
    implementation("io.ktor:ktor-client-encoding")
    implementation(npmConstrained("resolve-pkg"))
    implementation(npmConstrained("read-pkg-up"))

    testImplementation(kotlin("test"))
    testImplementation(npmConstrained("react"))
    testImplementation("com.zegreatrob.testmints:standard")
    testImplementation("com.zegreatrob.testmints:async")
    testImplementation("com.zegreatrob.testmints:minassert")
}

val cdnLookupConfiguration: Configuration by configurations.creating

val outputFile: String? by project

tasks {
    named("nodeRun", NodeJsExec::class) {
        this.args("react")
        outputFile?.let {
            standardOutput = file("${System.getProperty("user.dir")}/$it").outputStream()
        }
    }
}

artifacts {
    val task = tasks.named("compileProductionExecutableKotlinJs", KotlinJsIrLink::class)
    add(cdnLookupConfiguration.name, task.map {
        it.destinationDirectory.file(it.compilerOptions.moduleName.map { "$it.js" })
    }) { builtBy(task) }
}
