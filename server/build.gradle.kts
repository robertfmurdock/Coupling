import com.moowork.gradle.node.yarn.YarnTask

plugins {
    id("com.moowork.node")
}

tasks {
    task("clean") {
        doLast {
            delete(file("../build"))
        }
    }

    task<YarnTask>("serverCompile") {
        dependsOn(":yarn")
        mustRunAfter("clean")
        inputs.dir("../node_modules")
        inputs.file(file("../package.json"))
        inputs.file(file("../tsconfig.json"))
        inputs.file(file("webpack.config.js"))
        inputs.dir("./")
        inputs.dir("../common")
        outputs.dir(file("../build"))
        setEnvironment(mapOf(Pair("NODE_ENV", "production")))
        args = listOf("webpack", "--config", "server/webpack.config.js")
    }

    task<YarnTask>("serverTest") {
        dependsOn(":yarn")
        inputs.file(file("../package.json"))
        inputs.files(getByName("serverCompile").inputs.files)
        inputs.dir("../test/unit/server")
        outputs.dir("../test-output/server.unit")

        args = listOf("run", "serverTest", "--silent")
    }

    task<YarnTask>("endpointTest") {
        dependsOn(":yarn", "serverCompile")
        mustRunAfter("serverTest")
        inputs.files(getByName("serverTest").inputs.files)
        inputs.files(getByName("serverCompile").outputs.files)
        inputs.file(file("../package.json"))
        inputs.dir("../test/endpoint")
        outputs.dir("../test-output/endpoint")

        args = listOf("run", "endpointTest", "--silent")
    }
}
