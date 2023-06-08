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
    implementation(project("action"))
    implementation(project(":libraries:action"))
    implementation(project(":libraries:json"))
    implementation(project(":libraries:model"))
    implementation(project(":libraries:repository:core"))
    implementation("com.zegreatrob.jsmints:minreact")
    implementation("com.zegreatrob.jsmints:react-data-loader")
    implementation("com.zegreatrob.testmints:action")
    implementation("com.zegreatrob.testmints:action-async")
    implementation("com.zegreatrob.testmints:minspy")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom")
    implementation(npmConstrained("react-websocket"))
    implementation(npmConstrained("fitty"))
    implementation(npmConstrained("blueimp-md5"))
    implementation(npmConstrained("date-fns"))
    implementation(npmConstrained("react-dnd"))
    implementation(npmConstrained("react-dnd-html5-backend"))
    implementation(npmConstrained("html2canvas"))

    testImplementation(project(":libraries:test-react"))
    testImplementation(project(":libraries:stub-model"))
    testImplementation(project(":libraries:test-logging"))
    testImplementation("com.zegreatrob.jsmints:minenzyme")
    testImplementation("com.zegreatrob.testmints:async")
}
