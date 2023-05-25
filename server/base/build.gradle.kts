plugins {
    id("com.zegreatrob.coupling.plugins.versioning")
}

tasks {
    val buildImage by registering(Exec::class) {
        commandLine(
            "docker build --tag ghcr.io/robertfmurdock/coupling-serverless-base:latest ."
                .split(" ")
        )
    }
    register("pushImage", Exec::class) {
        mustRunAfter(buildImage)
        commandLine(
            "docker push ghcr.io/robertfmurdock/coupling-serverless-base:latest"
                .split(" ")
        )
    }
    register("pullImage", Exec::class) {
        commandLine(
            "docker pull ghcr.io/robertfmurdock/coupling-serverless-base:latest"
                .split(" ")
        )
    }
}
