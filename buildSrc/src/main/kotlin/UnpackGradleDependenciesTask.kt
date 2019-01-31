package org.jetbrains.kotlin.gradle.frontend.npm

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation
import org.jetbrains.kotlin.utils.LibraryUtils
import java.io.File

/**
 * @author Hat tip to Sergey Mashkov, needed to adopt and tweak this because the frontend plugin isn't ready
 */
open class UnpackGradleDependenciesTask : DefaultTask() {
    @Internal
    lateinit var dependenciesProvider: () -> List<Dependency>

    var customCompileConfiguration: List<Configuration> = emptyList()

    var customTestCompileConfiguration: List<Configuration> = emptyList()

    @get:Input
    val compileConfigurations: List<File>
        get() = customCompileConfiguration.flatten()

    @get:Input
    val testCompileConfigurations: List<File>
        get() = customTestCompileConfiguration.flatten()

    @OutputFile
    val resultFile = unpackFile(project)

    @Internal
    var resultNames: MutableList<NameVersionsUri>? = null

    @TaskAction
    fun unpackLibraries() {
        resultNames = mutableListOf()
        val out = project.buildDir.resolve("node_modules_imported")

        out.mkdirs()

        var mainConfigurations = customCompileConfiguration
        var testConfigurations = customTestCompileConfiguration

        val additionalConfigs = mainConfigurations.getProjectDependencyConfigs() + testConfigurations.getProjectDependencyConfigs()

        mainConfigurations = mainConfigurations.plus(additionalConfigs.map { it.main }.flatten())
        testConfigurations = testConfigurations.plus(additionalConfigs.map { it.test }.flatten())


        (mainConfigurations.map { it.resolvedConfiguration.resolvedArtifacts }.flatten() +
                testConfigurations.map { it.resolvedConfiguration.resolvedArtifacts }.flatten()
                )
                .filter { it.file.exists() && LibraryUtils.isKotlinJavascriptLibrary(it.file) }
                .forEach { artifact ->

                    @Suppress("UNCHECKED_CAST")
                    val existingPackageJson = project.zipTree(artifact.file).firstOrNull { it.name == "package.json" }?.let { JsonSlurper().parse(it) as Map<String, Any> }

                    if (existingPackageJson != null) {
                        val name = existingPackageJson["name"]?.toString()
                                ?: getJsModuleName(artifact.file)
                                ?: artifact.name
                                ?: artifact.id.displayName
                                ?: artifact.file.nameWithoutExtension

                        val outDir = out.resolve(name)
                        outDir.mkdirs()

                        logger.debug("Unpack to node_modules from ${artifact.file} to $outDir")
                        project.copy { copy ->
                            copy.from(project.zipTree(artifact.file))
                                    .into(outDir)
                        }

                        val existingVersion = existingPackageJson["version"]?.toString() ?: "0.0.0"

                        resultNames?.add(NameVersionsUri(name, artifact.moduleVersion.id.version, existingVersion, outDir.toLocalURI()))
                    } else {
                        val modules = getJsModuleNames(artifact.file)
                                .takeIf { it.isNotEmpty() } ?: listOf(
                                artifact.name
                                        ?: artifact.id.displayName
                                        ?: artifact.file.nameWithoutExtension
                        )

                        for (name in modules) {
                            val version = artifact.moduleVersion.id.version

                            val outDir = out.resolve(name)
                            outDir.mkdirs()

                            logger.debug("Unpack to node_modules from ${artifact.file} to $outDir")
                            project.copy { copy ->
                                copy.from(project.zipTree(artifact.file))
                                        .into(outDir)
                            }

                            val packageJson = mapOf(
                                    "name" to name,
                                    "version" to version,
                                    "main" to "$name.js",
                                    "_source" to "gradle"
                            )

                            outDir.resolve("package.json").bufferedWriter().use { out ->
                                out.appendln(JsonBuilder(packageJson).toPrettyString())
                            }

                            resultNames?.add(NameVersionsUri(name, artifact.moduleVersion.id.version, version, outDir.toLocalURI()))
                        }
                    }
                }

        resultFile.bufferedWriter().use { writer -> resultNames?.joinTo(writer, separator = "\n", postfix = "\n") { "${it.name}/${it.version}/${it.semver}/${it.uri}" } }
    }

    private fun List<Configuration>.getProjectDependencyConfigs() = asSequence()
            .map { it.allDependencies }
            .flatten()
            .filterIsInstance<ProjectDependency>()
            .map { it.dependencyProject }
            .toSet()
            .map { dependencyProject -> forEachJsTarget(dependencyProject) }

    data class NameVersionsUri(val name: String, val version: String, val semver: String, val uri: String)

    private val moduleNamePattern = """\s*//\s*Kotlin\.kotlin_module_metadata\(\s*\d+\s*,\s*("[^"]+")""".toRegex()
    private fun getJsModuleName(file: File): String? {
        return project.zipTree(file)
                .filter { it.name.endsWith(".meta.js") && it.canRead() }
                .mapNotNull { moduleNamePattern.find(it.readText())?.groupValues?.get(1) }
                .mapNotNull { JsonSlurper().parseText(it)?.toString() }
                .singleOrNull()
    }

    private fun getJsModuleNames(file: File): List<String> {
        return project.zipTree(file)
                .filter { it.name.endsWith(".meta.js") && it.canRead() }
                .mapNotNull { moduleNamePattern.find(it.readText())?.groupValues?.get(1) }
                .mapNotNull { JsonSlurper().parseText(it)?.toString() }
    }

    companion object {
        fun unpackFile(project: Project) = project.buildDir.resolve(".unpack.txt")
    }
}

fun File.toLocalURI() = toURI().toASCIIString().replaceFirst("file:[/]+".toRegex(), "file:///")

fun forEachJsTarget(project: Project) = project.multiPlatformExtension
        ?.javascriptTarget()
        ?.mainAndTest()
        ?: MainAndTestKtConfiguration(emptyList(), emptyList())

private fun KotlinTarget.mainAndTest(): MainAndTestKtConfiguration {
    val mainCompilation = emptyList<KotlinJsCompilation?>().toMutableList()
    val testCompilation = emptyList<KotlinJsCompilation?>().toMutableList()
    this.compilations.forEach { compilation ->
        when (compilation.name) {
            KotlinCompilation.MAIN_COMPILATION_NAME -> mainCompilation += (compilation as? KotlinJsCompilation)
            KotlinCompilation.TEST_COMPILATION_NAME -> testCompilation += (compilation as? KotlinJsCompilation)
        }
    }
    return MainAndTestKtConfiguration(
            mainCompilation.filterNotNull().findConfigurations(project),
            testCompilation.filterNotNull().findConfigurations(project)
    )
}

private fun KotlinMultiplatformExtension.javascriptTarget() = targets
        .firstOrNull { it.platformType == KotlinPlatformType.js }

data class MainAndTestKtConfiguration(val main: List<Configuration>, val test: List<Configuration>)

val Project.multiPlatformExtension
    get(): KotlinMultiplatformExtension? =
        project.extensions.findByName("kotlin") as? KotlinMultiplatformExtension

fun List<KotlinJsCompilation>.findConfigurations(project: Project) =
        map { project.configurations.getByName(it.compileDependencyConfigurationName) }