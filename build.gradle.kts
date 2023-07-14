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


    nested("caddy").apply {
        setProjectName("Coupling-root")
        startedServices.set(listOf("caddy"))
        waitForTcpPorts.set(false)
    }
}

tagger {
    releaseBranch = "master"
    githubReleaseEnabled.set(true)
}

tasks {
    named("composeUp") {
        mustRunAfter("caddyComposeUp", "libraries:repository:dynamo:composeUp")
        dependsOn(":server:buildImage")
    }
    register("importCert", Exec::class) {
        dependsOn("caddyComposeUp")
        val cert = "${System.getenv("HOME")}/caddy_data/caddy/pki/authorities/local/root.crt"
        commandLine(
            ("keytool -importcert -file $cert -alias $cert -cacerts -storepass changeit -noprompt")
                .split(" ")
        )
        isIgnoreExitValue = true
    }
}
