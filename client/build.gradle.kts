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

val nodeEnv = System.getenv("COUPLING_NODE_ENV") ?: "production"

tasks {

    task("clean") {
        doLast {
            delete(file("build"))
        }
    }

    task<YarnTask>("vendorCompile") {
        dependsOn("yarn", ":commonKt:build")
        inputs.dir("node_modules")
        inputs.file(file("package.json"))
        inputs.file(file("vendor.webpack.config.js"))
        outputs.dir("build/lib/vendor")
        setEnvironment(mapOf("NODE_ENV" to nodeEnv))
        args = listOf("webpack", "--config", "vendor.webpack.config.js")
    }

    task<YarnTask>("compile") {
        dependsOn("yarn", "vendorCompile", ":commonKt:build")
        inputs.dir("node_modules")
        inputs.file(file("package.json"))
        inputs.file(file("webpack.config.js"))
        inputs.file(file("../tsconfig.json"))
        inputs.dir("../common")
        outputs.dir("build/lib")
        inputs.dir("./")
        setEnvironment(mapOf("NODE_ENV" to nodeEnv))
        args = listOf("webpack", "--config", "webpack.config.js")
    }

    task<YarnTask>("test") {
        dependsOn("yarn", "vendorCompile", ":engine:build")
        inputs.file(file("package.json"))
        inputs.files(findByName("vendorCompile")?.inputs?.files)
        inputs.files(findByPath(":engine:assemble")?.outputs?.files)
        inputs.dir("test")
        outputs.dir(file("build/test-results"))

        args = listOf("run", "test", "--silent")
    }

    task<YarnTask>("testWatch") {
        args = listOf("run", "testWatch")
    }

    task<YarnTask>("stats") {
        dependsOn("yarn", "vendorCompile")

        setEnvironment(mapOf("NODE_ENV" to nodeEnv))
        args = listOf("-s", "webpack", "--json", "--profile", "--config", "webpack.config.js")

        setExecOverrides(closureOf<ExecSpec> {
            file("build/report").mkdirs()
            standardOutput = FileOutputStream(file("build/report/stats.json"))
        })
    }

    task<YarnTask>("vendorStats") {
        dependsOn("yarn", ":engine:build")
        setEnvironment(mapOf("NODE_ENV" to nodeEnv))
        args = listOf("-s", "webpack", "--json", "--profile", "--config", "vendor.webpack.config.js")

        setExecOverrides(closureOf<ExecSpec> {
            file("build/report").mkdirs()
            standardOutput = FileOutputStream(file("build/report/vendor.stats.json"))
        })
    }

    forEach { if (it.name != "clean") it.mustRunAfter("clean") }
}