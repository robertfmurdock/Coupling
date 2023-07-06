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
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/js/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/js/test/kotlin")
    }
}

tasks {
    formatKotlinMain {
        dependsOn("kspKotlinJs")
    }
    lintKotlinMain {
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
    implementation(project("action"))
    implementation(project(":libraries:action"))
    implementation(project(":libraries:json"))
    implementation(project(":libraries:model"))
    implementation(project(":libraries:repository:core"))
    implementation("com.zegreatrob.jsmints:minreact")
    implementation("com.zegreatrob.jsmints:react-data-loader")
    implementation("com.zegreatrob.testmints:action")
    implementation("com.zegreatrob.testmints:action-async")
    implementation("com.zegreatrob.testmints:minspy")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom")
    implementation(npmConstrained("react-use-websocket"))
    implementation(npmConstrained("fitty"))
    implementation(npmConstrained("blueimp-md5"))
    implementation(npmConstrained("date-fns"))
    implementation(npmConstrained("react-dnd"))
    implementation(npmConstrained("react-dnd-html5-backend"))
    implementation(npmConstrained("html2canvas"))

    testImplementation(project(":libraries:test-react"))
    testImplementation(project(":libraries:stub-model"))
    testImplementation(project(":libraries:test-logging"))
    testImplementation("com.zegreatrob.testmints:async")
}
