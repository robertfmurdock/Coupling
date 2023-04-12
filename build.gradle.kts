
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.ProjectLocalConfigurations
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsCompilerAttribute
import java.time.Duration

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("com.zegreatrob.coupling.plugins.versioning")
    id("com.zegreatrob.coupling.plugins.linter")
    alias(libs.plugins.com.avast.gradle.docker.compose)
    alias(libs.plugins.com.github.sghill.distribution.sha)
    alias(libs.plugins.com.github.ben.manes.versions)
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
    base
}

dockerCompose {
    setProjectName("Coupling-root")
    tcpPortsToIgnoreWhenWaiting.set(listOf(5555))
    startedServices.set(listOf("serverless", "caddy", "dynamo"))
    containerLogToDir.set(project.file("build/test-output/containers-logs"))
    waitForTcpPorts.set(false)
    waitAfterHealthyStateProbeFailure.set(Duration.ofMillis(100))
}

tagger {
    releaseBranch = "master"
}
//
// tasks {
//     named("composeUp") {
//         dependsOn(":server:buildImage")
//     }
// }
//
// val appConfiguration: Configuration by configurations.creating {
//     attributes {
//         attribute(
//             KotlinJsCompilerAttribute.jsCompilerAttribute,
//             KotlinJsCompilerAttribute.ir
//         )
//         attribute(
//             ProjectLocalConfigurations.ATTRIBUTE,
//             ProjectLocalConfigurations.PUBLIC_VALUE
//         )
//         attribute(
//             KotlinPlatformType.attribute,
//             KotlinPlatformType.js
//         )
//     }
// }
//
// dependencies {
//     appConfiguration(project(mapOf("path" to ":server", "configuration" to "appConfiguration")))
// }
