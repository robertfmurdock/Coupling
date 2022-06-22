plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
}

repositories {
    mavenCentral()
}

kotlin {
    js {
        nodejs()
        compilations.named("test") {
            packageJson {
                customField("mocha", mapOf("require" to "global-jsdom/register"))
            }
        }
    }
}
dependencies {
    api("org.jetbrains.kotlin-wrappers:kotlin-react")
    api("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
    api("com.zegreatrob.jsmints:minreact")

    api("org.jetbrains.kotlin:kotlin-test")
    api("com.zegreatrob.testmints:standard")
    api("com.zegreatrob.testmints:minassert")
}
