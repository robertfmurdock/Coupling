import com.zegreatrob.tools.tagger.ReleaseVersion

plugins {
    application
    id("com.zegreatrob.coupling.plugins.jvm")
    kotlin("plugin.serialization")
}

application {
    mainClass.set("com.zegreatrob.coupling.cli.MainKt")
}

dependencies {
    implementation(project(":libraries:auth0-management"))
    implementation(project(":sdk"))
    implementation(libs.com.github.ajalt.clikt.clikt)
    implementation("com.benasher44:uuid")
    implementation("com.zegreatrob.tools:digger-json")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-java")
    implementation("io.ktor:ktor-client-logging")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    implementation("org.slf4j:slf4j-api")
    implementation("org.slf4j:slf4j-simple")
}

tasks {
    distTar {
        compression = Compression.GZIP
    }
    val uploadToS3 by registering(Exec::class) {
        dependsOn(distTar)
        if (("${rootProject.version}").run { contains("SNAPSHOT") || isBlank() }) {
            enabled = false
        }
        val absolutePath = distTar.get().destinationDirectory.get().asFile.absolutePath
        commandLine = "aws s3 sync $absolutePath s3://assets.zegreatrob.com/coupling-cli/${rootProject.version}".split(" ")
    }
    rootProject
        .tasks
        .withType(ReleaseVersion::class.java)
        .named("release")
        .configure {
            finalizedBy(uploadToS3)
        }
}
