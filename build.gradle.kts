import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.DockerPullImage
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage
import com.moowork.gradle.node.task.NodeTask
import com.moowork.gradle.node.yarn.YarnInstallTask
import com.moowork.gradle.node.yarn.YarnSetupTask
import com.moowork.gradle.node.yarn.YarnTask
import com.zegreatrob.coupling.build.BuildConstants

plugins {
    id("com.github.node-gradle.node") version "1.3.0"
    id("com.bmuschko.docker-remote-api") version "4.2.0"
}

node {
    version = BuildConstants.nodeVersion
    npmVersion = BuildConstants.npmVersion
    yarnVersion = BuildConstants.yarnVersion
    download = true
}


tasks {
    val clean by creating {
        doLast {
            delete(file("build"))
            delete(file("test-output"))
        }
    }

    val yarn by getting {
        inputs.file(file("package.json"))
        outputs.dir(file("node_modules"))
    }

    val copyServerIcons by creating(Copy::class) {
        from("server/public")
        into("build/public")
    }

    val copyServerViews by creating(Copy::class) {
        from("server/views")
        into("build/views")
    }

    val copyServerResources by creating {
        dependsOn(copyServerIcons, copyServerViews)
    }

    val copyClient by creating(Copy::class) {
        dependsOn(":client:compile", copyServerResources)
        from("client/build/lib")
        into("build/public/app/build")
    }

    val copyClientTestResults by creating(Copy::class) {
        from("client/build/test-results")
        into("test-output/client")
    }

    val copyCommonKtTestResults by creating(Copy::class) {
        from("commonKt/build/test-results/jsTest")
        into("test-output/commonKt")
    }

    val copyServerTestResults by creating(Copy::class) {
        from("server/build/test-results")
        into("test-output")
    }

    val copyTestResultsForCircle by creating {
        dependsOn(copyClientTestResults, copyServerTestResults, copyCommonKtTestResults)
    }

    val serverCompile by creating(YarnTask::class) {
        dependsOn(yarn, copyServerResources, ":engine:assemble")
        mustRunAfter(clean)
        inputs.dir("node_modules")
        inputs.files(findByPath(":engine:assemble")?.outputs?.files)
        inputs.file(file("package.json"))
        inputs.file(file("tsconfig.json"))
        inputs.file(file("server/webpack.config.js"))
        inputs.dir("server/config")
        inputs.dir("server/lib")
        inputs.dir("server/public")
        inputs.dir("server/routes")
        inputs.dir("server/views")
        inputs.file("server/app.ts")
        inputs.file("server/routes.ts")
        inputs.dir("common")
        outputs.dir(file("build"))
        setEnvironment(mapOf("NODE_ENV" to "production"))
        args = listOf("webpack", "--config", "server/webpack.config.js")
    }

    val compile by creating {
        dependsOn(serverCompile, copyClient)
    }

    val serverTest by creating(YarnTask::class) {
        dependsOn(yarn, ":engine:assemble", ":engine:jsTest", ":test-style:assemble")
        inputs.file(file("package.json"))
        inputs.files(serverCompile.inputs.files)
        inputs.dir("server/test/unit")
        outputs.dir("server/build/test-results/server.unit")

        args = listOf("run", "serverTest", "--silent")
    }

    val endpointTest by creating(YarnTask::class) {
        dependsOn(yarn, serverCompile)
        mustRunAfter(serverTest)
        inputs.files(serverTest.inputs.files)
        inputs.files(serverCompile.outputs.files)
        inputs.file(file("package.json"))
        inputs.dir("server/test/endpoint")
        outputs.dir("test-output/endpoint")

        setEnvironment(mapOf("NODE_PATH" to "engine/build/node_modules_imported"))
        args = listOf("run", "endpointTest", "--silent")
    }

    val updateWebdriver by creating(YarnTask::class) {
        dependsOn("yarn")
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
        inputs.dir("test/e2e")
        outputs.dir("test-output/e2e")

        setEnvironment(mapOf("NODE_PATH" to "engine/build/node_modules_imported"))
        args = listOf("run", "protractor", "--silent", "--seleniumAddress", System.getenv("SELENIUM_ADDRESS") ?: "")
    }

    val test by creating {
        dependsOn(serverTest, ":client:test", endpointTest)
    }

    val check by creating {
        dependsOn(test)
    }

    val start by creating(YarnTask::class) {
        dependsOn(compile)
        args = listOf("run", "start-built-app")
    }

    val testWatch by creating(NodeTask::class) {
        setArgs(listOf("server/test/continuous-run.js"))
    }

    val build by creating {
        dependsOn(test, endToEndTest, ":client:compile")
    }

    val pullProductionImage by creating(DockerPullImage::class) {
        repository.set("zegreatrob/coupling")
        tag.set("latest")
    }

    val buildProductionImage by creating(DockerBuildImage::class) {
        mustRunAfter("pullProductionImage")
        inputDir.set(file("./"))
        dockerFile.set(file("Dockerfile.prod"))
        remove.set(false)
        tags.add("zegreatrob/coupling")
    }

    val pushProductionImage by creating(DockerPushImage::class) {
        mustRunAfter("buildProductionImage")
        imageName.set("zegreatrob/coupling")
        tag.set("latest")
    }
}


docker {
    registryCredentials {
        username.set(System.getenv("DOCKER_USER"))
        password.set(System.getenv("DOCKER_PASS"))
        email.set(System.getenv("DOCKER_EMAIL"))
    }
}

afterEvaluate {
    val installTasks = getAllTasks(true)
            .map { (_, tasks) -> tasks }
            .flatten()
            .filterIsInstance<YarnInstallTask>()

    installTasks.zipWithNext { a: YarnInstallTask, b: YarnInstallTask -> a.mustRunAfter(b) }

    val setupTasks = getAllTasks(true)
            .map { (_, tasks) -> tasks }
            .flatten()
            .filterIsInstance<YarnSetupTask>()
    setupTasks.zipWithNext { a: YarnSetupTask, b: YarnSetupTask -> a.mustRunAfter(b) }

    installTasks.forEach { installTask ->
        setupTasks.forEach { setupTask ->
            installTask.mustRunAfter(setupTask)
        }
    }
}