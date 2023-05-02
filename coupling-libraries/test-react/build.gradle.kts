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
    api("com.zegreatrob.jsmints:minreact")
    api("com.zegreatrob.jsmints:react-testing-library")
    api("com.zegreatrob.jsmints:user-event-testing-library")
    api("com.zegreatrob.testmints:minassert")
    api("com.zegreatrob.testmints:standard")
    api("org.jetbrains.kotlin-wrappers:kotlin-react")
    api("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
    api("org.jetbrains.kotlin:kotlin-test")
    api(npmConstrained("jsdom"))
    api(npmConstrained("global-jsdom"))
}
