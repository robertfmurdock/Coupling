import com.moowork.gradle.node.yarn.YarnTask
import java.io.FileOutputStream

plugins {
    id("com.github.node-gradle.node")
}

node {
    version = "11.6.0"
    npmVersion = "6.5.0"
    yarnVersion = "1.12.3"
    download = true
}

tasks {

    task("clean") {
        doLast {
            delete(file("build"))
        }
    }

    task<YarnTask>("vendorCompile") {
        dependsOn("yarn")
        inputs.dir("node_modules")
        inputs.file(file("package.json"))
        inputs.file(file("vendor.webpack.config.js"))
        outputs.dir("build/lib/vendor")
        setEnvironment(mapOf("NODE_ENV" to "production"))
        args = listOf("webpack", "--config", "vendor.webpack.config.js")
    }

    task<YarnTask>("compile") {
        dependsOn("yarn", "vendorCompile")
        inputs.dir("node_modules")
        inputs.file(file("package.json"))
        inputs.file(file("webpack.config.js"))
        inputs.file(file("../tsconfig.json"))
        inputs.dir("../common")
        outputs.dir("build/lib")
        inputs.dir("./")
        setEnvironment(mapOf("NODE_ENV" to "production"))
        args = listOf("webpack", "--config", "webpack.config.js")
    }

    task<YarnTask>("test") {
        dependsOn("yarn", "vendorCompile")
        inputs.file(file("package.json"))
        inputs.files(findByName("vendorCompile")?.inputs?.files)
        inputs.dir("test")
        outputs.dir(file("build/test-results"))

        args = listOf("run", "clientTest", "--silent")
    }

    task<YarnTask>("stats") {
        dependsOn("yarn", "vendorCompile")

        setEnvironment(mapOf("NODE_ENV" to "production"))
        args = listOf("-s", "webpack", "--json", "--config", "webpack.config.js")

        setExecOverrides(closureOf<ExecSpec> {
            file("build/report").mkdirs()
            standardOutput = FileOutputStream(file("build/report/stats.json"))
        })
    }

    task<YarnTask>("vendorStats") {
        setEnvironment(mapOf("NODE_ENV" to "production"))
        args = listOf("-s", "webpack", "--json", "--config", "vendor.webpack.config.js")

        setExecOverrides(closureOf<ExecSpec> {
            file("build/report").mkdirs()
            standardOutput = FileOutputStream(file("build/reports/vendor.stats.json"))
        })
    }

    forEach { if (it.name != "clean") it.mustRunAfter("clean") }
}