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

        target.extensions.configure(com.moowork.gradle.node.NodeExtension::class.java) {
            it.version = BuildConstants.nodeVersion
            it.npmVersion = BuildConstants.npmVersion
            it.yarnVersion = BuildConstants.yarnVersion
            it.download = true
        }

        target.tasks.run {
            val unpackJsGradleDependencies = create("unpackJsGradleDependencies", UnpackGradleDependenciesTask::class.java)

            create("jasmine", NodeTask::class.java) {
                it.dependsOn("yarn", unpackJsGradleDependencies)
                val script = target.rootDir.path + "/buildSrc/test-wrapper.js"
                it.inputs.file(script)
                it.inputs.file(target.file("package.json"))

                it.setScript(java.io.File(script))
                it.outputs.dir("build/test-results/jsTest")
            }
        }

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

                inputs.files(kotlinCompileTestTask.outputFile)

                relevantPaths.forEach { if (java.io.File(it).isDirectory) inputs.dir(it) }

                setEnvironment(mapOf("NODE_PATH" to relevantPaths.joinToString(":")))

                setArgs(listOf("${kotlinCompileTestTask.outputFile}"))
            }

            val test = target.tasks.findByName("test")
                    ?: target.tasks.create("test")


            val jsTest = target.tasks.findByName("jsTest")
                    ?: target.tasks.create("jsTest")
            jsTest.run {
                dependsOn(jasmine)
                test.dependsOn(this)
            }
        }

    }
}