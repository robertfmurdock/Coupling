plugins {
    id("com.zegreatrob.coupling.plugins.mp")
}
kotlin {
    jvm()
    js {
        nodejs()
        useCommonJs()
    }
}

dependencies {
    commonMainApi(project(":libraries:model"))
    commonMainApi("com.benasher44:uuid")
}