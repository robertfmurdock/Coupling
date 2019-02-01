
import com.moowork.gradle.node.task.NodeTask
import com.moowork.gradle.node.yarn.YarnTask
import com.zegreatrob.coupling.build.BuildConstants

plugins {
    id("com.github.node-gradle.node")
}

node {
    version = BuildConstants.nodeVersion
    npmVersion = BuildConstants.npmVersion
    yarnVersion = BuildConstants.yarnVersion
    download = true
}

tasks {

    val yarn by getting {
        inputs.file(file("package.json"))
        outputs.dir(file("node_modules"))
    }
    
    val clean by creating {
        doLast {
            delete(file("build"))
        }
    }

    val copyServerIcons by creating(Copy::class) {
        from("public")
        into("build/executable/public")
    }

    val copyServerViews by creating(Copy::class) {
        from("views")
        into("build/executable/views")
    }

    val copyServerResources by creating {
        dependsOn(copyServerIcons, copyServerViews)
    }

    val copyClient by creating(Copy::class) {
        dependsOn(":client:compile", copyServerResources)
        from("../client/build/lib")
        into("build/executable/public/app/build")
    }

    val serverCompile by creating(YarnTask::class) {
        dependsOn(yarn, copyServerResources, ":engine:assemble")
        mustRunAfter(clean)
        inputs.dir("node_modules")
        inputs.files(findByPath(":engine:assemble")?.outputs?.files)
        inputs.file(file("package.json"))
        inputs.file(file("../tsconfig.json"))
        inputs.file(file("webpack.config.js"))
        inputs.dir("config")
        inputs.dir("lib")
        inputs.dir("public")
        inputs.dir("routes")
        inputs.dir("views")
        inputs.file("app.ts")
        inputs.file("routes.ts")
        inputs.dir("../common")
        outputs.dir(file("build/executable"))
        setEnvironment(mapOf("NODE_ENV" to "production"))
        args = listOf("webpack", "--config", "webpack.config.js")
    }

    val compile by creating {
        dependsOn(serverCompile, copyClient)
    }

    val serverTest by creating(YarnTask::class) {
        dependsOn(yarn, ":engine:assemble", ":engine:jsTest", ":test-style:assemble")
        inputs.file(file("package.json"))
        inputs.files(serverCompile.inputs.files)
        inputs.dir("test/unit")
        outputs.dir("build/executable/test-results/server.unit")

        args = listOf("run", "serverTest", "--silent")
    }

    val endpointTest by creating(YarnTask::class) {
        dependsOn(yarn, serverCompile)
        mustRunAfter(serverTest)
        inputs.files(serverTest.inputs.files)
        inputs.files(serverCompile.outputs.files)
        inputs.file(file("package.json"))
        inputs.dir("test/endpoint")
        outputs.dir("../test-output/endpoint")

        setEnvironment(mapOf("NODE_PATH" to "../engine/build/node_modules_imported"))
        args = listOf("run", "endpointTest", "--silent")
    }

    val updateWebdriver by creating(YarnTask::class) {
        dependsOn(yarn)
        outputs.dir("node_modules/webdriver-manager/selenium/")
        args = listOf("run", "update-webdriver", "--silent")
    }

    val endToEndTest by creating(YarnTask::class) {
        dependsOn(compile, updateWebdriver)
        mustRunAfter(serverTest, ":client:test", endpointTest)
        inputs.files(findByPath(":client:test")?.inputs?.files)
        inputs.files(findByPath(":client:compile")?.outputs?.files)
        inputs.files(serverTest.inputs.files)
        inputs.files(serverCompile.outputs.files)
        inputs.file(file("package.json"))
        inputs.dir("../test/e2e")
        outputs.dir("../test-output/e2e")

        setEnvironment(mapOf("NODE_PATH" to "../engine/build/node_modules_imported"))
        args = listOf("run", "protractor", "--silent", "--seleniumAddress", System.getenv("SELENIUM_ADDRESS") ?: "")
    }

    val test by creating {
        dependsOn(serverTest, endpointTest)
    }

    val start by creating(YarnTask::class) {
        dependsOn(compile)
        args = listOf("run", "start-built-app")
    }

    val testWatch by creating(NodeTask::class) {
        setArgs(listOf("test/continuous-run.js"))
    }

}