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
    implementation(project(":coupling-libraries:action"))
    implementation(project(":coupling-libraries:json"))
    implementation(project(":coupling-libraries:model"))
    implementation(project(":coupling-libraries:repository-core"))
    implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom")
    implementation("com.zegreatrob.testmints:action")
    implementation("com.zegreatrob.testmints:action-async")
    implementation("com.zegreatrob.testmints:minspy")
    implementation("com.zegreatrob.jsmints:minreact")
    implementation("com.zegreatrob.jsmints:react-data-loader")
    implementation(npmConstrained("react-websocket"))
    implementation(npmConstrained("fitty"))
    implementation(npmConstrained("blueimp-md5"))
    implementation(npmConstrained("date-fns"))
    implementation(npmConstrained("react-dnd"))
    implementation(npmConstrained("react-dnd-html5-backend"))

    testImplementation(project(":coupling-libraries:test-react"))
    testImplementation(project(":coupling-libraries:stub-model"))
    testImplementation(project(":coupling-libraries:test-logging"))
    testImplementation("com.zegreatrob.testmints:async")
    testImplementation("com.zegreatrob.jsmints:minenzyme")
    testImplementation(npmConstrained("@testing-library/react"))
    testImplementation(npmConstrained("@testing-library/user-event"))
}
