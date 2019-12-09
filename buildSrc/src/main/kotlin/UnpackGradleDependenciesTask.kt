package com.zegreatrob.coupling.build

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleJavaTargetExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.utils.LibraryUtils
import java.io.File
import kotlin.reflect.full.declaredMemberProperties

fun File.toLocalURI() = toURI().toASCIIString().replaceFirst("file:[/]+".toRegex(), "file:///")

fun forEachJsTarget(project: Project) = project.javascriptTarget()
    ?.mainAndTest()
    ?: MainAndTestKtConfiguration(emptyList(), emptyList())

private fun Project.javascriptTarget(): KotlinTarget? = multiPlatformExtension
    ?.javascriptTarget()
    ?: kotlinExtension?.javascriptTarget()

private fun KotlinSingleJavaTargetExtension.javascriptTarget() = this.internalTarget

private fun KotlinTarget.mainAndTest(): MainAndTestKtConfiguration {
    val mainCompilation = emptyList<KotlinCompilation<*>?>().toMutableList()
    val testCompilation = emptyList<KotlinCompilation<*>?>().toMutableList()
    this.compilations.forEach { compilation ->
        when (compilation.name) {
            KotlinCompilation.MAIN_COMPILATION_NAME -> mainCompilation += compilation
            KotlinCompilation.TEST_COMPILATION_NAME -> testCompilation += compilation
        }
    }
    if (mainCompilation.isEmpty()) {
        throw Error("Could not find any kotlin compilations for project ${project.name}")
    }

    return MainAndTestKtConfiguration(
        mainCompilation.filterNotNull().findConfigurations(project)
            .apply { if (isEmpty()) throw Error("Could not find main configurations for project ${project.name}") }
        ,
        testCompilation.filterNotNull().findConfigurations(project)
    )
}

private fun KotlinMultiplatformExtension.javascriptTarget() = targets
    .firstOrNull { it.platformType == KotlinPlatformType.js }

data class MainAndTestKtConfiguration(val main: List<Configuration>, val test: List<Configuration>)

val Project.multiPlatformExtension
    get(): KotlinMultiplatformExtension? =
        project.extensions.findByName("kotlin") as? KotlinMultiplatformExtension

private val Project.kotlinExtension: KotlinSingleJavaTargetExtension?
    get() = extensions.findByName("kotlin") as? KotlinSingleJavaTargetExtension

private val KotlinSingleJavaTargetExtension.internalTarget: KotlinTarget
    get() = KotlinSingleJavaTargetExtension::class.declaredMemberProperties.find { it.name == "target" }!!.get(this) as KotlinTarget

fun List<KotlinCompilation<*>>.findConfigurations(project: Project) =
    map { project.configurations.getByName(it.compileDependencyConfigurationName) }