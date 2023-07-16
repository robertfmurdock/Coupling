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

dependencies {
    commonMainApi(project(":libraries:logging"))
    commonMainApi("com.zegreatrob.testmints:standard")
    commonMainApi("com.zegreatrob.testmints:report")
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-datetime")
    commonMainImplementation("org.jetbrains.kotlin:kotlin-stdlib-common")
    commonMainImplementation("io.github.microutils:kotlin-logging")
    commonMainImplementation("org.jetbrains.kotlin:kotlin-test")
}

artifacts {
    val compileProductionExecutableKotlinJs =
        tasks.named("compileProductionExecutableKotlinJs", Kotlin2JsCompile::class)
    add(testLoggingLib.name, compileProductionExecutableKotlinJs.map {
        it.destinationDirectory.file(it.compilerOptions.moduleName.map { "$it.js" })
    }) {
        builtBy(compileProductionExecutableKotlinJs)
    }
}
