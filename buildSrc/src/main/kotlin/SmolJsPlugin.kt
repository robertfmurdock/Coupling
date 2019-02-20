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

            val kotlinTestTask = compileKotlinJsTasks.find { it.name == "compileTestKotlinJs" }
                    ?: compileKotlinJsTasks.find { it.name == "compileTestKotlin2Js" }
                    ?: throw Exception("Could not find kotlin test task.")

            val processResources = target.tasks.filterIsInstance(ProcessResources::class.java)
            target.tasks.getByName("jasmine")
                    .run {
                        dependsOn(compileKotlinJsTasks)
                        dependsOn(processResources)

                        inputs.file(kotlinTestTask.outputFile)
                    }
        }

        target.tasks.run {
            val unpackJsGradleDependencies = create("unpackJsGradleDependencies", UnpackGradleDependenciesTask::class.java)

            create("jasmine", NodeTask::class.java) {
                it.dependsOn("yarn", unpackJsGradleDependencies)
//                val relevantPaths = listOf(
//                        "node_modules",
//                        "build/node_modules_imported",
//                        compileKotlinJs.outputFile.parent,
//                        jsTestProcessResources.destinationDir
//                )
//
//                inputs.file(compileTestKotlinJs.outputFile)
//
                val script = target.rootDir.path + "/buildSrc/test-wrapper.js"
                it.inputs.file(script)
                it.setScript(java.io.File(script))

//                relevantPaths.forEach { inputs.dir(it) }
//
//                setEnvironment(mapOf("NODE_PATH" to relevantPaths.joinToString(":")))
//
//                setArgs(listOf("${compileTestKotlinJs.outputFile}"))
//
//                outputs.dir("build/test-results/jsTest")
//
            }
        }
    }


}