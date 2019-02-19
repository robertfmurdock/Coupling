package com.zegreatrob.coupling.build

import com.moowork.gradle.node.NodePlugin
import com.moowork.gradle.node.task.NodeTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class SmolJsPlugin : Plugin<Project> {

    override fun apply(target: Project) {

        target.pluginManager.apply(NodePlugin::class.java)

//        target.configure(target, object : Closure<Unit>(target) {
//            override fun call(arguments: Any?) {

//                println("$arguments")

//                println("WHAAAT")
//            }
//        })

        target.afterEvaluate {
            target.tasks.filterIsInstance(UnpackGradleDependenciesTask::class.java)
                    .forEach {
                        forEachJsTarget(target).let { (main, test) ->
                            it.customCompileConfiguration = main
                            it.customTestCompileConfiguration = test
                        }
                    }
        }

        target.tasks.run {
            val unpackJsGradleDependencies = create("unpackJsGradleDependencies", UnpackGradleDependenciesTask::class.java)

            create("jasmine", NodeTask::class.java) {
                it.dependsOn("yarn", unpackJsGradleDependencies)
                //                dependsOn(yarn, compileKotlinJs, compileTestKotlinJs, unpackJsGradleDependencies)
//                mustRunAfter(compileTestKotlinJs, jsTestProcessResources)
//
//                val relevantPaths = listOf(
//                        "node_modules",
//                        "build/node_modules_imported",
//                        compileKotlinJs.outputFile.parent,
//                        jsTestProcessResources.destinationDir
//                )
//
//                inputs.file(compileTestKotlinJs.outputFile)
//
//                val script = file("test-wrapper.js")
//
//                inputs.file(script)
//                setScript(script)
//
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