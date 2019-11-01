
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.DockerPullImage
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("com.github.node-gradle.node") apply false
    id("com.bmuschko.docker-remote-api") version "5.3.0"
    id("com.github.ben-manes.versions") version "0.27.0"
    id("net.rdrei.android.buildtimetracker") version "0.11.0"
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        maven { url = uri("https://dl.bintray.com/robertfmurdock/zegreatrob") }
        maven { url = uri("https://dl.bintray.com/soywiz/soywiz") }
        maven { url = uri("https://kotlin.bintray.com/kotlinx") }
        maven { url = uri("http://dl.bintray.com/kotlin/kotlin-js-wrappers") }
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
    val clean by creating {
        doLast {
            delete(file("build"))
            delete(file("test-output"))
        }
    }

    val copyClientTestResults by creating(Copy::class, copyForTask(findByPath(":client:test")) {
        from("client/build/test-results")
        into("build/test-output/client")
    })

    val copyActionTestResults by creating(Copy::class, copyForTask(findByPath(":action:jsTest")) {
        from("action/build/test-results/jsTest")
        into("build/test-output/action")
    })

    val copyServerTestResults by creating(Copy::class, copyForTask(findByPath(":server:serverTest")) {
        from("server/build/test-results/server")
        into("build/test-output/server")
    })

    val copyEndpointTestResults by creating(Copy::class, copyForTask(findByPath(":server:endpointTest")) {
        from("server/build/test-results/endpoint")
        into("build/test-output/endpoint")
    })

    val copyEngineTestResults by creating(Copy::class, copyForTask(findByPath(":server:engine:jsTest")) {
        from("engine/build/test-results/jsTest")
        into("build/test-output/engine")
    })

    val copyEndToEndResults by creating(Copy::class, copyForTask(findByPath(":server:endToEndTest")) {
        from("server/build/test-results/e2e")
        into("build/test-output/e2e")
    })

    val copyEndToEndScreenshotResults by creating(Copy::class, copyForTask(findByPath(":server:endToEndTest")) {
        from("server/build/reports/e2e")
        into("build/test-output/e2e-screenshots")
    })

    val copyTestResultsForCircle by creating {
        dependsOn(
            copyClientTestResults,
            copyServerTestResults,
            copyEndpointTestResults,
            copyActionTestResults,
            copyEngineTestResults,
            copyEndToEndResults,
            copyEndToEndScreenshotResults
        )
    }

    val test by creating {
        dependsOn(":server:test", ":client:test")
    }

    val check by creating {
        dependsOn(test, ":server:endpointTest", ":server:endToEndTest")
    }

    val build by creating {
        dependsOn(test, ":client:compile", ":server:build")
    }

    val pullProductionImage by creating(DockerPullImage::class) {
        repository.set("zegreatrob/coupling")
        tag.set("latest")
    }

    val buildProductionImage by creating(DockerBuildImage::class) {
        mustRunAfter("pullProductionImage")
        inputDir.set(file("./"))
        dockerFile.set(file("Dockerfile.prod"))
        remove.set(false)
        tags.add("zegreatrob/coupling")
    }

    val pushProductionImage by creating(DockerPushImage::class) {
        mustRunAfter("buildProductionImage")
        imageName.set("zegreatrob/coupling")
        tag.set("latest")
    }

    val serverYarn = getByPath(":server:yarn")
    val clientYarn = getByPath(":client:yarn")
    serverYarn.mustRunAfter(clientYarn)
    val commonYarn = getByPath(":action:yarn")
    commonYarn.mustRunAfter(serverYarn)
    val coreYarn = getByPath(":model:yarn")
    coreYarn.mustRunAfter(commonYarn)
    val engineYarn = getByPath(":server:engine:yarn")
    engineYarn.mustRunAfter(coreYarn)
    val coreJsonYarn = getByPath(":json:yarn")
    coreJsonYarn.mustRunAfter(engineYarn)
    val coreMongoYarn = getByPath(":mongo:yarn")
    coreMongoYarn.mustRunAfter(coreJsonYarn)
}

fun copyForTask(testTask: Task?, block: Copy.() -> Unit): Copy.() -> Unit {
    return {
        mustRunAfter(testTask)

        block()
        testTask?.finalizedBy(this)
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
}