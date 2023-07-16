import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    id("com.zegreatrob.coupling.plugins.mp")
    id("com.zegreatrob.testmints.action-mint")
}
kotlin {
    targets {
        js {
            moduleName = "Coupling-server-action"
            nodejs()
            useCommonJs()
        }
    }
    sourceSets {
        getByName("commonMain") {
            dependencies {
                api(project(":libraries:repository:core"))
                api(project(":libraries:model"))
                api(project(":libraries:action"))
                api("com.zegreatrob.testmints:action")
                api("com.zegreatrob.testmints:action-async")
                implementation("com.benasher44:uuid")
                implementation("io.github.microutils:kotlin-logging")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
            }
        }
        getByName("commonTest") {
            dependencies {
                api(project(":libraries:stub-model"))
                api(project(":libraries:test-action"))
                implementation(project(":libraries:repository:memory"))
                implementation("com.zegreatrob.testmints:async")
                implementation("com.zegreatrob.testmints:minassert")
                implementation("com.zegreatrob.testmints:minspy")
                implementation("com.zegreatrob.testmints:standard")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                implementation("org.jetbrains.kotlin:kotlin-test-common")
            }
        }

        getByName("jsTest") {
            dependencies {
                api(project(":libraries:logging"))
                implementation("org.jetbrains.kotlin:kotlin-test-js")
            }
        }
    }

    sourceSets.named("jsMain") {
        kotlin.srcDir("build/generated/ksp/js/jsMain/kotlin")
    }

}

tasks {
    "formatKotlinJsMain" {
        dependsOn("kspKotlinJs")
    }
    "lintKotlinJsMain" {
        dependsOn("kspKotlinJs")
    }
    withType(FormatTask::class) {
        exclude { spec -> spec.file.absolutePath.contains("generated") }
    }
    withType(LintTask::class) {
        exclude { spec -> spec.file.absolutePath.contains("generated") }
    }
}