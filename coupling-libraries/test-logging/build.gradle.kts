import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("com.zegreatrob.coupling.plugins.mp")
}

group = "com.zegreatrob.coupling.libraries"

kotlin {
    targets {
        jvm()
        js {
            nodejs()
            useCommonJs()
            binaries.executable()
        }
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                api(project(":coupling-libraries:logging"))
                api("com.zegreatrob.testmints:standard")
                api("com.zegreatrob.testmints:report")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
                implementation("io.github.microutils:kotlin-logging:2.1.21")
                implementation("com.soywiz.korlibs.klock:klock:2.7.0")
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("io.github.microutils:kotlin-logging:2.1.21")
                implementation(kotlin("reflect"))
                implementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
                implementation("org.slf4j:slf4j-simple:2.0.0-alpha6")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
            }
        }
    }
}

val testLoggingLib: Configuration by configurations.creating {

}

tasks {
    val compileProductionExecutableKotlinJs = named("compileProductionExecutableKotlinJs", Kotlin2JsCompile::class) {
        artifacts {
            add(testLoggingLib.name, outputFileProperty) {
                builtBy(this@named)
            }
        }
    }
    named("jsJar") {
        dependsOn(compileProductionExecutableKotlinJs)
    }
}