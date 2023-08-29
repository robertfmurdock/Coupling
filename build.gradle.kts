import com.avast.gradle.dockercompose.tasks.ComposeUp
import com.gradle.scan.plugin.internal.dep.com.fasterxml.jackson.databind.ObjectMapper
import java.io.ByteArrayOutputStream
import java.time.Duration

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("com.zegreatrob.coupling.plugins.versioning")
    id("com.zegreatrob.coupling.plugins.linter")
    alias(libs.plugins.com.avast.gradle.docker.compose)
    alias(libs.plugins.com.github.ben.manes.versions)
    alias(libs.plugins.com.github.sghill.distribution.sha)
    alias(libs.plugins.com.zegreatrob.tools.digger)
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

    val outputStream = ByteArrayOutputStream()
    exec {
        commandLine(
            "/bin/bash",
            "-c",
            "aws ssm get-parameters --names /prerelease/stripe_pk /prerelease/stripe_sk --with-decryption | jq '[.Parameters[].Value']"
        )
        standardOutput = outputStream
    }
    val (pk, sk) = outputStream.toByteArray().let { ObjectMapper().readValue(it, List::class.java) }
    environment.put("STRIPE_PUBLISHABLE_KEY", pk.toString())
    environment.put("STRIPE_SECRET_KEY", sk.toString())

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
    named<ComposeUp>("composeUp") {
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

    "versionCatalogUpdate" {
        dependsOn("libraries:js-dependencies:ncuUpgrade")
        dependsOn(provider { gradle.includedBuilds.map { it.task(":versionCatalogUpdate") } })
    }
}
