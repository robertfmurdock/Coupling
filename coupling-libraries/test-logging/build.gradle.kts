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
                implementation("io.github.microutils:kotlin-logging")
                implementation("com.soywiz.korlibs.klock:klock")
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
                api("io.github.microutils:kotlin-logging")
                implementation(kotlin("reflect"))
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.junit.jupiter:junit-jupiter-engine")
                implementation("org.slf4j:slf4j-simple")
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
