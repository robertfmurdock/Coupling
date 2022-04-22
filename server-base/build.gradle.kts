plugins {
    id("com.zegreatrob.coupling.plugins.versioning")
    id("com.bmuschko.docker-remote-api")
}

tasks {
    val buildImage by registering(com.bmuschko.gradle.docker.tasks.image.DockerBuildImage::class) {
        inputDir.set(project.projectDir)
        images.add("ghcr.io/robertfmurdock/coupling-serverless-base:latest")
    }
    val pushImage by registering(com.bmuschko.gradle.docker.tasks.image.DockerPushImage::class) {
        mustRunAfter(buildImage)
        images.add("ghcr.io/robertfmurdock/coupling-serverless-base:latest")
    }
    val pullImage by registering(com.bmuschko.gradle.docker.tasks.image.DockerPullImage::class) {
        image.set("ghcr.io/robertfmurdock/coupling-serverless-base:latest")
    }
}
