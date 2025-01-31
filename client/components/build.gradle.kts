plugins {
    id("com.zegreatrob.jsmints.plugins.minreact")
    id("com.zegreatrob.jsmints.plugins.wrapper")
    id("com.zegreatrob.coupling.plugins.jstools")
    alias(libs.plugins.io.github.turansky.seskar)
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
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react-popper")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react-router")
    jsMainImplementation(npmConstrained("@nivo/bar"))
    jsMainImplementation(npmConstrained("@nivo/boxplot"))
    jsMainImplementation(npmConstrained("@nivo/core"))
    jsMainImplementation(npmConstrained("@nivo/heatmap"))
    jsMainImplementation(npmConstrained("@nivo/line"))
    jsMainImplementation(npmConstrained("@stripe/react-stripe-js"))
    jsMainImplementation(npmConstrained("@stripe/stripe-js"))
    jsMainImplementation(npmConstrained("d3-array"))
    jsMainImplementation(npmConstrained("date-fns"))
    jsMainImplementation(npmConstrained("fitty"))
    jsMainImplementation(npmConstrained("html2canvas"))
    jsMainImplementation(npmConstrained("marked"))
    jsMainImplementation(npmConstrained("react-dnd"))
    jsMainImplementation(npmConstrained("react-dnd-html5-backend"))
    jsMainImplementation(npmConstrained("react-use-websocket"))
    jsMainImplementation(npmConstrained("recharts"))
    jsMainImplementation(npmConstrained("ws"))

    jsTestImplementation(project(":libraries:test-react"))
    jsTestImplementation(project(":libraries:test-action"))
    jsTestImplementation(project(":libraries:stub-model"))
    jsTestImplementation(project(":libraries:test-logging"))
    jsTestImplementation("com.zegreatrob.testmints:async")
    jsTestImplementation(npmConstrained("jsdom"))
    jsTestImplementation(npmConstrained("global-jsdom"))
}
