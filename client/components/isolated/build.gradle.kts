
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
    jsMainImplementation("com.zegreatrob.jsmints:minreact")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-emotion-styled")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
    jsMainImplementation(project(":libraries:model"))
    jsMainImplementation(project(":libraries:action"))

    jsTestImplementation(project(":libraries:stub-model"))
    jsTestImplementation(project(":libraries:test-action"))
    jsTestImplementation(project(":libraries:test-logging"))
    jsTestImplementation(project(":libraries:test-react"))
    jsTestImplementation("com.zegreatrob.testmints:async")
    jsTestImplementation("com.zegreatrob.testmints:minspy")
    jsTestImplementation("com.zegreatrob.testmints:minassert")
    jsTestImplementation(npmConstrained("jsdom"))
    jsTestImplementation(npmConstrained("global-jsdom"))
}
