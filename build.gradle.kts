import de.gliderpilot.gradle.semanticrelease.SemanticReleaseChangeLogService
import org.ajoberstar.gradle.git.release.semver.ChangeScope
import org.ajoberstar.grgit.Commit
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.ProjectLocalConfigurations
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsCompilerAttribute
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn

plugins {
    id("com.bmuschko.docker-remote-api") version "7.1.0"
    id("net.rdrei.android.buildtimetracker") version "0.11.0"
    id("de.gliderpilot.semantic-release") version "1.4.2"
    id("com.avast.gradle.docker-compose") version "0.14.11"
    id("com.zegreatrob.coupling.plugins.versioning")
}

semanticRelease {
    changeLog(closureOf<SemanticReleaseChangeLogService> {
        changeScope = KotlinClosure1<Commit, ChangeScope>({ ChangeScope.PATCH })
    })
}

dockerCompose {
    tcpPortsToIgnoreWhenWaiting.set(listOf(5555))
    if (System.getenv("CI") != null) {
        startedServices.set(listOf("serverless", "dynamo", "caddy"))
    }
    containerLogToDir.set(project.file("build/test-output/containers-logs"))
}

tasks {
    val composeUp by getting {
        dependsOn(":server:buildImage")
    }
}

yarn.ignoreScripts = false

docker {
    registryCredentials {
        username.set(System.getenv("DOCKER_USER"))
        password.set(System.getenv("DOCKER_PASS"))
        email.set(System.getenv("DOCKER_EMAIL"))
    }
}

val appConfiguration: Configuration by configurations.creating {
    attributes {
        attribute(
            KotlinJsCompilerAttribute.jsCompilerAttribute,
            KotlinJsCompilerAttribute.ir
        )
        attribute(
            ProjectLocalConfigurations.ATTRIBUTE,
            ProjectLocalConfigurations.PUBLIC_VALUE
        )
        attribute(
            KotlinPlatformType.attribute,
            KotlinPlatformType.js
        )
    }
}

dependencies {
    appConfiguration(project(mapOf("path" to ":server", "configuration" to "appConfiguration")))
}

buildtimetracker {
    reporters {
        register("csv") {
            options.run {
                put("output", "${buildDir.absolutePath}/times.csv")
                put("append", "true")
                put("header", "false")
            }
        }

        register("summary") {
            options.run {
                put("ordered", "false")
                put("threshold", "50")
                put("header", "false")
            }
        }

        register("csvSummary") {
            options.run {
                put("csv", "${buildDir.absolutePath}/times.csv")
            }
        }
    }
}
