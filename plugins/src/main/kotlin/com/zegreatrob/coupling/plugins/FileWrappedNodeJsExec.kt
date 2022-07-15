package com.zegreatrob.coupling.plugins

import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

open class FileWrappedNodeJsExec
@Inject
constructor(
    @Internal
    override val compilation: KotlinJsCompilation,
) : NodeJsExec(compilation) {
    @OutputFile
    lateinit var outputFile: File

    override fun exec() {
        standardOutput = ByteArrayOutputStream()
        super.exec()
        outputFile.writeText(standardOutput.toString())
    }
}
