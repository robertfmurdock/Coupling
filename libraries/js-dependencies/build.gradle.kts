plugins {
    id("com.zegreatrob.jsmints.plugins.ncu")
    id("org.jmailen.kotlinter")
}

repositories {
    mavenCentral()
}

kotlin { js { nodejs() } }
