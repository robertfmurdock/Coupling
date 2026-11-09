import com.zegreatrob.coupling.plugins.conventions.KotlinConventions

plugins {
    id("com.zegreatrob.jsmints.plugins.ncu")
    id("org.jmailen.kotlinter")
}

repositories {
    mavenCentral()
}

KotlinConventions.configureNodeRuntime(project)

kotlin { js { nodejs() } }
