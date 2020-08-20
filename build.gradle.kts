
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.DockerPullImage
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.zegreatrob.coupling.build.JsonLoggingTestListener
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest

plugins {
    id("com.github.node-gradle.node") apply false
    id("com.bmuschko.docker-remote-api") version "6.6.1"
    id("se.patrikerdes.use-latest-versions") version "0.2.14"
    id("com.github.ben-manes.versions") version "0.29.0"
    id("net.rdrei.android.buildtimetracker") version "0.11.0"
}

allprojects {
    apply(plugin = "se.patrikerdes.use-latest-versions")
    apply(plugin = "com.github.ben-manes.versions")    
    repositories {
        mavenCentral()
        jcenter()
        maven { url = uri("https://kotlin.bintray.com/kotlinx") }
        maven { url = uri("https://dl.bintray.com/robertfmurdock/zegreatrob") }
        maven { url = uri("https://dl.bintray.com/kotlin/kotlin-js-wrappers") }
    }

    tasks {
        val copyReportsToCircleCIDirectory by creating(Copy::class) {
            from("build/reports")
            into("${rootProject.buildDir.path}/test-output/${project.path}")
        }

        withType<DependencyUpdatesTask> {
            checkForGradleUpdate = true
            outputFormatter = "json"
            outputDir = "build/dependencyUpdates"
            reportfileName = "report"
            revision = "release"
        }
    }

    afterEvaluate {
        mkdir(file(rootProject.buildDir.toPath().resolve("test-output")))
        tasks.withType(KotlinJsTest::class) {
            addTestListener(JsonLoggingTestListener(path))
        }
    }
}

docker {
    registryCredentials {
        username.set(System.getenv("DOCKER_USER"))
        password.set(System.getenv("DOCKER_PASS"))
        email.set(System.getenv("DOCKER_EMAIL"))
    }
}

tasks {
    val copyClientTestResults by creating(Copy::class, getByPath(":client:test").copyForTask {
        from("client/build/test-results")
        into("build/test-output/client")
    })

    val copyActionTestResults by creating(Copy::class, getByPath(":action:check").copyForTask {
        from("action/build/test-results/jsTest")
        into("build/test-output/action")
    })

    val copyEndpointTestResults by creating(Copy::class, getByPath(":sdk:endpointTest").copyForTask {
        from("sdk/build/test-results/jsTest")
        into("build/test-output/endpoint")
    })

    val copyEngineTestResults by creating(Copy::class, getByPath(":server:server_action:jsTest").copyForTask {
        from("engine/build/test-results/jsTest")
        into("build/test-output/engine")
    })

    val copyEndToEndResults by creating(Copy::class, getByPath(":e2e:wdioRun").copyForTask {
        from("e2e/build/logs")
        into("build/test-output/e2e/logs")
    })

    val copyEndToEndScreenshotResults by creating(Copy::class, getByPath(":e2e:wdioRun").copyForTask {
        from("e2e/build/reports/e2e")
        into("build/test-output/e2e/reports")
    })

    val copyTestResultsForCircle by creating {
        dependsOn(
            copyClientTestResults,
            copyEndpointTestResults,
            copyActionTestResults,
            copyEngineTestResults,
            copyEndToEndResults,
            copyEndToEndScreenshotResults
        )
    }

    val pullProductionImage by creating(DockerPullImage::class) {
        image.set("zegreatrob/coupling:latest")
    }

    val buildProductionImage by creating(DockerBuildImage::class) {
        mustRunAfter("pullProductionImage")
        inputDir.set(file("./"))
        dockerFile.set(file("Dockerfile.prod"))
        remove.set(false)
        images.add("zegreatrob/coupling:latest")
    }

    val pushProductionImage by creating(DockerPushImage::class) {
        mustRunAfter("buildProductionImage")
        images.add("zegreatrob/coupling:latest")
    }

    val test by creating {
        dependsOn(":server:test", ":client:test")
    }

    val check by getting {
        dependsOn(test, ":sdk:endpointTest")
    }

    val build by getting {
        dependsOn(test, ":client:assemble", ":server:build")
    }

    val kotlinNpmInstall by getting(KotlinNpmInstallTask::class) {

    }

}

fun Task.copyForTask(block: Copy.() -> Unit): Copy.() -> Unit {
    return {
        mustRunAfter(this@copyForTask)

        block()
        this@copyForTask.finalizedBy(this)
    }
}

buildtimetracker {
    reporters {
        register("csv") {
            options.run {
                put("output", "build/times.csv")
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
                put("csv", "build/times.csv")
            }
        }
    }
}

tasks.withType<DependencyUpdatesTask> {
    checkForGradleUpdate = true
    outputFormatter = "json"
    outputDir = "build/dependencyUpdates"
    reportfileName = "report"
    revision = "release"
}
