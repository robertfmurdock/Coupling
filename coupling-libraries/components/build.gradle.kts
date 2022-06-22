plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
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
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion")
    implementation("com.zegreatrob.jsmints:minreact")
    implementation(project(":coupling-libraries:model"))
    implementation(project(":coupling-libraries:json"))

    testImplementation(project(":coupling-libraries:test-react"))
    testImplementation("com.zegreatrob.jsmints:minenzyme")
}
