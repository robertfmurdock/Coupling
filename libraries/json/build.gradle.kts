import com.zegreatrob.coupling.plugins.NodeExec
import com.zegreatrob.coupling.plugins.setup
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon
//import org.jmailen.gradle.kotlinter.tasks.FormatTask
//import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
    kotlin("plugin.serialization")
}
kotlin {
    jvm()
    js { nodejs() }
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        allWarningsAsErrors = false
    }
    sourceSets {
        commonMain { kotlin.srcDir("build/generated/codegen") }
    }
}

dependencies {
    commonMainApi(project(":libraries:model"))
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    commonMainImplementation("io.ktor:ktor-client-core")
    commonMainImplementation("io.ktor:ktor-serialization-kotlinx-json")
    commonMainImplementation("io.ktor:ktor-client-content-negotiation")
    commonMainImplementation("io.ktor:ktor-client-logging")

    commonTestImplementation(project(":libraries:test-logging"))
    commonTestImplementation(project(":libraries:stub-model"))
    commonTestImplementation("com.zegreatrob.testmints:standard")
    commonTestImplementation("com.zegreatrob.testmints:minassert")
    commonTestImplementation("org.jetbrains.kotlin:kotlin-test")

    "jvmMainImplementation"(kotlin("reflect"))
    "jvmTestImplementation"("org.junit.jupiter:junit-jupiter-api")
    "jvmTestImplementation"("org.junit.jupiter:junit-jupiter-engine")

    "jsTestImplementation"(npmConstrained("@graphql-codegen/cli"))
    "jsTestImplementation"(npmConstrained("@graphql-codegen/kotlin"))
    "jsTestImplementation"(npmConstrained("@graphql-codegen/java-common"))
    "jsTestImplementation"(npmConstrained("@graphql-codegen/plugin-helpers"))
    "jsTestImplementation"(npmConstrained("@graphql-codegen/visitor-plugin-common"))
    "jsTestImplementation"(npmConstrained("tslib"))
}

tasks {
    val gqlCodeGen by registering(NodeExec::class) {
        dependsOn(
            "jsPackageJson",
            ":kotlinNpmInstall",
        )
        setup(project)
        nodeCommand = "graphql-codegen"
        inputs.file("codegen.yml")
        inputs.dir("../../server/src/jsMain/resources")
        outputs.dir(file("build/generated/codegen"))
    }
    withType(KotlinCompileCommon::class) {
        dependsOn(gqlCodeGen)
    }
    withType(KotlinCompile::class) {
        dependsOn(gqlCodeGen)
    }
    withType(Kotlin2JsCompile::class) {
        dependsOn(gqlCodeGen)
    }
//    withType<FormatTask> {
//        dependsOn(gqlCodeGen)
//    }
//    withType<LintTask> {
//        dependsOn(gqlCodeGen)
//    }
}
