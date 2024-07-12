plugins {
    id("com.zegreatrob.jsmints.plugins.minreact")
    id("com.zegreatrob.jsmints.plugins.wrapper")
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

tasks {
    formatKotlinJsMain {
        dependsOn("kspKotlinJs")
    }
    lintKotlinJsMain {
        dependsOn("kspKotlinJs")
    }
}

dependencies {
    jsMainApi(project("external"))
    jsMainImplementation(project(":libraries:action"))
    jsMainImplementation(project(":libraries:json"))
    jsMainImplementation(project(":libraries:model"))
    jsMainImplementation(project(":libraries:repository:core"))
    jsMainImplementation("com.zegreatrob.jsmints:minreact")
    jsMainImplementation("com.zegreatrob.jsmints:react-data-loader")
    jsMainImplementation("com.zegreatrob.testmints:action")
    jsMainImplementation("com.zegreatrob.testmints:action-async")
    jsMainImplementation("com.zegreatrob.testmints:minspy")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-emotion")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react-router")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom")
    jsMainImplementation(npmConstrained("@stripe/react-stripe-js"))
    jsMainImplementation(npmConstrained("@stripe/stripe-js"))
    jsMainImplementation(npmConstrained("date-fns"))
    jsMainImplementation(npmConstrained("fitty"))
    jsMainImplementation(npmConstrained("html2canvas"))
    jsMainImplementation(npmConstrained("react-dnd"))
    jsMainImplementation(npmConstrained("react-dnd-html5-backend"))
    jsMainImplementation(npmConstrained("react-use-websocket"))

    jsTestImplementation(project(":libraries:test-react"))
    jsTestImplementation(project(":libraries:test-action"))
    jsTestImplementation(project(":libraries:stub-model"))
    jsTestImplementation(project(":libraries:test-logging"))
    jsTestImplementation("com.zegreatrob.testmints:async")
    jsTestImplementation(npmConstrained("jsdom"))
    jsTestImplementation(npmConstrained("global-jsdom"))
}
