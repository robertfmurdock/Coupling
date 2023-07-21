plugins {
    id("com.zegreatrob.coupling.plugins.mp")
}
kotlin {
    targets {
        jvm()
        js {
            nodejs()
            useCommonJs()
        }
    }
}

dependencies {
    commonMainApi(project(":libraries:model"))
    commonMainApi("com.benasher44:uuid")
}