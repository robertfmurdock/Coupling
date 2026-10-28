import nl.littlerobots.vcu.plugin.versionSelector
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

repositories {
    maven { url = uri("https://plugins.gradle.org/m2/") }
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.version.catalog.update)
}

kotlin {
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

tasks {
    withType<FormatTask> {
        exclude { spec -> spec.file.absolutePath.contains("generated") }
    }
    withType<LintTask> {
        exclude { spec -> spec.file.absolutePath.contains("generated") }
    }
}

dependencies {
    implementation("com.zegreatrob.jsmints:plugins")
    implementation("com.zegreatrob.testmints:mint-logs-plugin")
    implementation("com.zegreatrob.testmints:action-mint-plugin")
    implementation("com.zegreatrob.tools:tagger-plugin")
    implementation("com.zegreatrob.tools:digger-plugin")
    implementation(kotlin("gradle-plugin", libs.versions.kotlin.get()))
    implementation(libs.jackson.databind)
    implementation(libs.kfc.gradle.plugin)
    implementation(libs.version.catalog.update.plugin)
    implementation(libs.log4j.core)
    implementation(libs.log4j.iostreams)
    implementation(libs.kotlin.js.plain.objects)
    implementation(libs.kotlin.serialization)
    implementation(libs.kotlinter.gradle)
    implementation(libs.slf4j.api)
    implementation(platform(libs.jsmints.bom))
    implementation(platform(libs.testmints.bom))
    implementation(platform(libs.tools.bom))

    testImplementation(kotlin("test"))

    constraints {
        implementation(libs.ksp.symbol.processing.api) {
            version {
                strictly(libs.versions.ksp.get())
            }
        }
        implementation(libs.ksp.symbol.processing.gradle.plugin) {
            version {
                strictly(libs.versions.ksp.get())
            }
        }
    }
}

versionCatalogUpdate {
    sortByKey = true
    val rejectRegex = "^[0-9.]+[0-9](-RC|-M[0-9]*|-RC[0-9]*.*|-beta.*|-Beta.*|-alpha.*|-dev.*|-rc\\.[0-9]*.*|-rc[0-9]*)$".toRegex()
    versionSelector { versionCandidate ->
        !rejectRegex.matches(versionCandidate.candidate.version)
    }
}
