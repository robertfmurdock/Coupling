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
    jsMainApi("com.zegreatrob.jsmints:minreact")
    jsMainApi("com.zegreatrob.jsmints:react-testing-library")
    jsMainApi("com.zegreatrob.jsmints:user-event-testing-library")
    jsMainApi("com.zegreatrob.testmints:minassert")
    jsMainApi("com.zegreatrob.testmints:standard")
    jsMainApi("org.jetbrains.kotlin-wrappers:kotlin-react")
    jsMainApi("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
    jsMainApi("org.jetbrains.kotlin:kotlin-test")
    jsMainImplementation(npmConstrained("jsdom"))
    jsMainImplementation(npmConstrained("global-jsdom"))
}
