import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.DockerPullImage
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage

plugins {
    id("com.github.node-gradle.node") apply false
    id("com.bmuschko.docker-remote-api") version "4.6.1"
    id("com.github.ben-manes.versions") version "0.21.0"
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        maven { url = uri("https://dl.bintray.com/robertfmurdock/zegreatrob") }
        maven { url = uri("https://dl.bintray.com/soywiz/soywiz") }
        maven { url = uri("https://kotlin.bintray.com/kotlinx") }
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

    val copyCommonKtTestResults by creating(Copy::class, copyForTask(findByPath(":commonKt:jsTest")) {
        from("commonKt/build/test-results/jsTest")
        into("build/test-output/commonKt")
    })

    val copyServerTestResults by creating(Copy::class, copyForTask(findByPath(":server:serverTest")) {
        from("server/build/test-results/server")
        into("build/test-output/server")
    })

    val copyEndpointTestResults by creating(Copy::class, copyForTask(findByPath(":server:endpointTest")) {
        from("server/build/test-results/endpoint")
        into("build/test-output/endpoint")
    })

    val copyEngineTestResults by creating(Copy::class, copyForTask(findByPath(":engine:jsTest")) {
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
                copyCommonKtTestResults,
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
    val commonYarn = getByPath(":commonKt:yarn")
    clientYarn.mustRunAfter(commonYarn)
    val engineYarn = getByPath(":engine:yarn")
    commonYarn.mustRunAfter(engineYarn)

}

fun copyForTask(testTask: Task?, block: Copy.() -> Unit): Copy.() -> Unit {
    return {
        mustRunAfter(testTask)

        block()
        testTask?.finalizedBy(this)
    }
}