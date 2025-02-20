plugins {
    id("com.zegreatrob.coupling.plugins.mp")
}
kotlin {
    jvm()
    js {
        nodejs()
        useCommonJs()
        compilerOptions { target = "es2015" }
    }
}

dependencies {
    commonMainImplementation(project(":libraries:model"))
}
