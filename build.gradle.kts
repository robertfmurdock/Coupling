import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.DockerPullImage
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage

plugins {
    id("com.github.node-gradle.node") version "1.3.0" apply false
    id("com.bmuschko.docker-remote-api") version "4.4.1"
    id("com.github.ben-manes.versions") version "0.20.0"
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

    val copyClientTestResults by creating(Copy::class) {
        dependsOn(":client:test")

        from("client/build/test-results")
        into("build/test-output/client")
    }

    val copyCommonKtTestResults by creating(Copy::class) {
        dependsOn(":commonKt:jsTest")
        from("commonKt/build/test-results/jsTest")
        into("build/test-output/commonKt")
    }

    val copyServerTestResults by creating(Copy::class) {
        dependsOn(":server:test")
        from("server/build/test-results")
        into("build/test-output")
    }

    val copyEngineTestResults by creating(Copy::class) {
        dependsOn(":engine:jsTest")
        from("engine/build/test-results/jsTest")
        into("build/test-output/engine")
    }

    val copyEndToEndResults by creating(Copy::class) {
        dependsOn(":server:endToEndTest")
        from("test-output/e2e")
        into("build/test-output/e2e")
    }

    val copyTestResultsForCircle by creating {
        dependsOn(
                copyClientTestResults,
                copyServerTestResults,
                copyCommonKtTestResults,
                copyEngineTestResults,
                copyEndToEndResults
        )
    }

    val test by creating {
        dependsOn(":server:test", ":client:test")
    }

    val check by creating {
        dependsOn(test, copyTestResultsForCircle)
    }

    val build by creating {
        dependsOn(test, ":server:endToEndTest", ":client:compile")
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