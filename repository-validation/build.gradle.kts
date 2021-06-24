import com.zegreatrob.coupling.build.BuildConstants
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    targets {
        jvm()
        js {
            nodejs()
            useCommonJs()
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":repository-core"))
                api(project(":test-logging"))
                api(project(":stub-model"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
                implementation("com.zegreatrob.testmints:standard:4.1.3")
                implementation("com.zegreatrob.testmints:minassert:4.1.3")
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }

        val jvmMain by getting {
            dependencies {
                api(kotlin("reflect", BuildConstants.kotlinVersion))
                implementation(kotlin("reflect", BuildConstants.kotlinVersion))
                implementation("org.junit.jupiter:junit-jupiter-api:5.8.0-M1")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.8.0-M1")
            }
        }

        val jsMain by getting {
            dependencies {
                api("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
                implementation("com.zegreatrob.testmints:async:4.1.3")
            }
        }
    }
}

tasks {
    val compileKotlinJs by getting(Kotlin2JsCompile::class) {
        kotlinOptions.moduleKind = "commonjs"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
    }
}
