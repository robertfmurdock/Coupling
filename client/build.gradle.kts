import com.moowork.gradle.node.yarn.YarnTask
import java.io.FileOutputStream

plugins {
    id("com.github.node-gradle.node")
}

node {
    version = "11.6.0"
    npmVersion = "6.5.0"
    yarnVersion = "1.10.1"
    download = true
}

tasks {

    task("clean") {
        doLast {
            delete(file("../public/app/build"))
        }
    }

    task<YarnTask>("clientVendorCompile") {
        dependsOn("yarn")
        mustRunAfter("clean")
        inputs.dir("node_modules")
        inputs.file(file("package.json"))
        inputs.file(file("vendor.webpack.config.js"))
        outputs.dir("../public/app/build/vendor")
        setEnvironment(mapOf("NODE_ENV" to "production"))
        args = listOf("webpack", "--config", "vendor.webpack.config.js")
    }

    task<YarnTask>("clientCompile") {
        dependsOn("yarn", "clientVendorCompile")
        mustRunAfter("clean")
        inputs.dir("node_modules")
        inputs.file(file("package.json"))
        inputs.file(file("webpack.config.js"))
        inputs.file(file("../tsconfig.json"))
        inputs.dir("../common")
        outputs.dir("../public/app/build")
        inputs.dir("./")
        setEnvironment(mapOf("NODE_ENV" to "production"))
        args = listOf("webpack", "--config", "webpack.config.js")
    }

    task<YarnTask>("clientTest") {
        dependsOn("yarn", "clientVendorCompile")
        inputs.file(file("package.json"))
        inputs.files(findByName("clientVendorCompile")?.inputs?.files)
        inputs.dir("test")
        outputs.dir(file("../test-output/client"))

        args = listOf("run", "clientTest", "--silent")
    }

    task<YarnTask>("clientStats") {
        setEnvironment(mapOf("NODE_ENV" to "production"))
        args = listOf("webpack", "--json", "--config", "webpack.config.js")

        setExecOverrides(closureOf<ExecSpec> {
            standardOutput = FileOutputStream("logs/my.log")
        })

    }
}