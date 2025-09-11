
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    id("com.zegreatrob.jsmints.plugins.minreact")
    id("com.zegreatrob.jsmints.plugins.wrapper")
    id("com.zegreatrob.coupling.plugins.jstools")
}

kotlin {
    js {
        nodejs()
        compilerOptions { target = "es2015" }
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
    withType<FormatTask> {
        dependsOn("kspKotlinJs")
    }
    withType<LintTask> {
        dependsOn("kspKotlinJs")
    }
}

dependencies {
    jsMainImplementation(npmConstrained("react-flip-toolkit"))
}
