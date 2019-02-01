import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.DockerPullImage
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage
import com.moowork.gradle.node.yarn.YarnInstallTask
import com.moowork.gradle.node.yarn.YarnSetupTask

plugins {
    id("com.github.node-gradle.node") version "1.3.0" apply false
    id("com.bmuschko.docker-remote-api") version "4.2.0"
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
        into("test-output/client")
    }

    val copyCommonKtTestResults by creating(Copy::class) {
        dependsOn(":commonKt:jsTest")
        from("commonKt/build/test-results/jsTest")
        into("test-output/commonKt")
    }

    val copyServerTestResults by creating(Copy::class) {
        dependsOn(":server:test")
        from("server/build/test-results")
        into("test-output")
    }

    val copyEngineTestResults by creating(Copy::class) {
        dependsOn(":engine:jsTest")
        from("engine/build/test-results/jsTest")
        into("test-output/engine")
    }

    val copyTestResultsForCircle by creating {
        dependsOn(copyClientTestResults, copyServerTestResults, copyCommonKtTestResults, copyEngineTestResults)
    }

    val test by creating {
        dependsOn(":server:test", ":client:test")
    }

    val check by creating {
        dependsOn(test)
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
}

afterEvaluate {
    val installTasks = getAllTasks(true)
            .map { (_, tasks) -> tasks }
            .flatten()
            .filterIsInstance<YarnInstallTask>()

    installTasks.zipWithNext { a: YarnInstallTask, b: YarnInstallTask -> a.mustRunAfter(b) }

    val setupTasks = getAllTasks(true)
            .map { (_, tasks) -> tasks }
            .flatten()
            .filterIsInstance<YarnSetupTask>()
    setupTasks.zipWithNext { a: YarnSetupTask, b: YarnSetupTask -> a.mustRunAfter(b) }

    installTasks.forEach { installTask ->
        setupTasks.forEach { setupTask ->
            installTask.mustRunAfter(setupTask)
        }
    }
}