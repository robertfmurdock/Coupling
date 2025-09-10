
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    id("com.zegreatrob.jsmints.plugins.minreact")
    id("com.zegreatrob.jsmints.plugins.wrapper")
    id("com.zegreatrob.coupling.plugins.jstools")
    alias(libs.plugins.io.github.turansky.seskar)
}

kotlin {
    js {
        nodejs()
        useEsModules()
        compilerOptions { target = "es2015" }
        compilations.named("test") {
            packageJson {
                customField("mocha", mapOf("require" to "global-jsdom/register"))
            }
        }
    }
}

tasks {
    withType<FormatTask> {
        dependsOn("kspKotlinJs")
    }
    withType<LintTask> {
        dependsOn("kspKotlinJs")
    }
}

dependencies {
    jsMainImplementation(project(":libraries:json"))
    jsMainImplementation(project(":libraries:model"))
    jsMainImplementation("com.zegreatrob.jsmints:minreact")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-emotion-styled")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
    jsMainImplementation(npmConstrained("@nivo/bar"))
    jsMainImplementation(npmConstrained("@nivo/boxplot"))
    jsMainImplementation(npmConstrained("@nivo/core"))
    jsMainImplementation(npmConstrained("@nivo/heatmap"))
    jsMainImplementation(npmConstrained("@nivo/line"))
    jsMainImplementation(npmConstrained("d3"))
    jsMainImplementation(npmConstrained("d3-array"))
    jsMainImplementation(npmConstrained("d3-array"))
    jsMainImplementation(npmConstrained("d3-color"))
    jsMainImplementation(npmConstrained("d3-selection"))
    jsMainImplementation(npmConstrained("recharts"))

    jsTestImplementation(project(":libraries:stub-model"))
    jsTestImplementation(project(":libraries:test-action"))
    jsTestImplementation(project(":libraries:test-logging"))
    jsTestImplementation(project(":libraries:test-react"))
    jsTestImplementation("com.zegreatrob.testmints:async")
    jsTestImplementation("com.zegreatrob.testmints:minspy")
    jsTestImplementation(npmConstrained("jsdom"))
    jsTestImplementation(npmConstrained("global-jsdom"))
}
