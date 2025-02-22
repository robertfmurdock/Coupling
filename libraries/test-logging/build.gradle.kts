import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("com.zegreatrob.coupling.plugins.mp")
}

kotlin {
    jvm()
    js {
        nodejs()
        useCommonJs()
        binaries.executable()
    }
}

val testLoggingLib: Configuration by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    attributes {
        attribute(Attribute.of("com.zegreatrob.executable", String::class.java), "test-logging")
    }
}

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
    commonMainImplementation("org.jetbrains.kotlin:kotlin-test")

    commonTestImplementation("org.jetbrains.kotlin:kotlin-test")

    "jvmMainApi"("io.github.oshai:kotlin-logging")
    "jvmMainImplementation"(kotlin("reflect"))
    "jvmMainImplementation"("org.junit.jupiter:junit-jupiter-api")
    "jvmMainImplementation"("org.junit.jupiter:junit-jupiter-engine")
    "jvmMainImplementation"("org.slf4j:slf4j-simple")
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
