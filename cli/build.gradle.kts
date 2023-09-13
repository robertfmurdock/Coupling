import com.zegreatrob.tools.tagger.ReleaseVersion

plugins {
    application
    id("com.zegreatrob.coupling.plugins.mp")
    kotlin("plugin.serialization")
}

application {
    mainClass.set("com.zegreatrob.coupling.cli.MainKt")
}

kotlin {
    jvm { withJava() }
    js { nodejs { binaries.executable() } }
}

dependencies {
    commonMainImplementation(project(":libraries:auth0-management"))
    commonMainImplementation(project(":libraries:action"))
    commonMainImplementation(project(":libraries:model"))
    commonMainImplementation(project(":sdk"))
    commonMainImplementation(libs.com.github.ajalt.clikt.clikt)
    commonMainImplementation("com.benasher44:uuid")
    commonMainImplementation("com.zegreatrob.tools:digger-json")
    commonMainImplementation("io.ktor:ktor-client-content-negotiation")
    commonMainImplementation("io.ktor:ktor-client-core")
    commonMainImplementation("io.ktor:ktor-client-logging")
    commonMainImplementation("io.ktor:ktor-serialization-kotlinx-json")
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    "jsMainImplementation"("org.jetbrains.kotlin-wrappers:kotlin-node")
    "jvmMainImplementation"("org.slf4j:slf4j-api")
    "jvmMainImplementation"("org.slf4j:slf4j-simple")
}

version = rootProject.version

tasks {
    distTar {
        compression = Compression.GZIP
        archiveFileName.set("coupling-cli.tgz")
    }
    val uploadToS3 by registering(Exec::class) {
        dependsOn(distTar)
        if (("${rootProject.version}").run { contains("SNAPSHOT") || isBlank() }) {
            enabled = false
        }
        val absolutePath = distTar.get().destinationDirectory.get().asFile.absolutePath
        commandLine =
            "aws s3 sync $absolutePath s3://assets.zegreatrob.com/coupling-cli/${rootProject.version}".split(" ")
    }
    rootProject
        .tasks
        .withType(ReleaseVersion::class.java)
        .named("release")
        .configure {
            finalizedBy(uploadToS3)
        }
}
