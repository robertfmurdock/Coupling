import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    id("com.zegreatrob.jsmints.plugins.minreact")
    id("com.zegreatrob.coupling.plugins.jstools")
}

kotlin {
    js {
        nodejs()
        compilations.named("test") {
            packageJson {
                customField("mocha", mapOf("require" to "global-jsdom/register"))
            }
        }
    }
}

kotlin {
    sourceSets.jsMain {
        kotlin.srcDir("build/generated/ksp/js/jsMain/kotlin")
    }
}

tasks {
    formatKotlinJsMain {
        dependsOn("kspKotlinJs")
    }
    lintKotlinJsMain {
        dependsOn("kspKotlinJs")
    }
    withType(FormatTask::class) {
        exclude { spec -> spec.file.absolutePath.contains("generated") }
    }
    withType(LintTask::class) {
        exclude { spec -> spec.file.absolutePath.contains("generated") }
    }
}

dependencies {
    jsMainImplementation(project(":libraries:action"))
    jsMainImplementation(project(":libraries:json"))
    jsMainImplementation(project(":libraries:model"))
    jsMainImplementation(project(":libraries:repository:core"))
    jsMainImplementation("com.zegreatrob.jsmints:minreact")
    jsMainImplementation("com.zegreatrob.jsmints:react-data-loader")
    jsMainImplementation("com.zegreatrob.testmints:action")
    jsMainImplementation("com.zegreatrob.testmints:action-async")
    jsMainImplementation("com.zegreatrob.testmints:minspy")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-emotion")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom")
    jsMainImplementation(npmConstrained("react-use-websocket"))
    jsMainImplementation(npmConstrained("fitty"))
    jsMainImplementation(npmConstrained("blueimp-md5"))
    jsMainImplementation(npmConstrained("date-fns"))
    jsMainImplementation(npmConstrained("react-dnd"))
    jsMainImplementation(npmConstrained("react-dnd-html5-backend"))
    jsMainImplementation(npmConstrained("html2canvas"))

    jsTestImplementation(project(":libraries:test-react"))
    jsTestImplementation(project(":libraries:stub-model"))
    jsTestImplementation(project(":libraries:test-logging"))
    jsTestImplementation("com.zegreatrob.testmints:async")
    jsTestImplementation(npmConstrained("jsdom"))
    jsTestImplementation(npmConstrained("global-jsdom"))
}
