import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("com.zegreatrob.coupling.plugins.mp")
}

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
                implementation("io.github.microutils:kotlin-logging:2.1.23")
                implementation("com.soywiz.korlibs.klock:klock:3.0.1")
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }
        getByName("jvmMain") {
            dependencies {
                implementation("io.github.microutils:kotlin-logging:2.1.23")
                implementation(kotlin("reflect"))
                implementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.9.0")
                implementation("org.slf4j:slf4j-simple:2.0.0")
            }
        }
        getByName("jsMain") {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
            }
        }
    }
}

val testLoggingLib: Configuration by configurations.creating {}

tasks {
    named("jsJar") {
        dependsOn("compileProductionExecutableKotlinJs")
    }
}

artifacts {
    val compileProductionExecutableKotlinJs =
        tasks.named("compileProductionExecutableKotlinJs", Kotlin2JsCompile::class)
    add(testLoggingLib.name, compileProductionExecutableKotlinJs.map { it.outputFileProperty }) {
        builtBy(compileProductionExecutableKotlinJs)
    }
}
