package com.zegreatrob.coupling.build

import com.moowork.gradle.node.NodePlugin
import com.moowork.gradle.node.task.NodeTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

class SmolJsPlugin : Plugin<Project> {

    override fun apply(target: Project) {

        target.pluginManager.apply(NodePlugin::class.java)

        target.afterEvaluate {
            val assemble = target.tasks.findByName("assemble")
            assemble?.dependsOn("unpackJsGradleDependencies")

            val forEachJsTarget = forEachJsTarget(target)

            target.tasks.filterIsInstance(UnpackGradleDependenciesTask::class.java)
                    .forEach { unpackTask ->
                        forEachJsTarget.let { (main, test) ->
                            unpackTask.customCompileConfiguration = main
                            unpackTask.customTestCompileConfiguration = test
                        }
                    }

            val compileKotlinJsTasks = target.tasks.filterIsInstance(Kotlin2JsCompile::class.java)

            val kotlinCompileTestTask = compileKotlinJsTasks.find { it.name == "compileTestKotlinJs" }
                    ?: compileKotlinJsTasks.find { it.name == "compileTestKotlin2Js" }
                    ?: throw Exception("Could not find kotlin test task.")

            val processResources = target.tasks.filterIsInstance(ProcessResources::class.java)
            val jasmine = target.tasks.filterIsInstance(NodeTask::class.java).find { it.name == "jasmine" }
                    ?: throw Exception("Could not find Jasmine test task.")
            jasmine.run {
                dependsOn(compileKotlinJsTasks)
                dependsOn(processResources)

                val relevantPaths = listOf("node_modules", "build/node_modules_imported") +
                        compileKotlinJsTasks.map { it.outputFile.parent } +
                        processResources.map { it.destinationDir.path }

                inputs.file(kotlinCompileTestTask.outputFile)

                relevantPaths.forEach { if (java.io.File(it).isDirectory) inputs.dir(it) }

                setEnvironment(mapOf("NODE_PATH" to relevantPaths.joinToString(":")))

                setArgs(listOf("${kotlinCompileTestTask.outputFile}"))

                outputs.dir("build/test-results/jsTest")
            }

            val jsTest = target.tasks.getByName("jsTest") {
                it.dependsOn(jasmine)
            }

            target.tasks.create("test") {
                it.dependsOn(jsTest)
            }
        }

        target.tasks.run {
            val unpackJsGradleDependencies = create("unpackJsGradleDependencies", UnpackGradleDependenciesTask::class.java)

            val jasmine = create("jasmine", NodeTask::class.java) {
                it.dependsOn("yarn", unpackJsGradleDependencies)
                val script = target.rootDir.path + "/buildSrc/test-wrapper.js"
                it.inputs.file(script)
                it.setScript(java.io.File(script))
            }

        }
    }


}