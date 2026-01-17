package com.zegreatrob.coupling.cli

import java.nio.file.Files
import kotlin.io.path.absolutePathString

actual fun createTempDirectory(): String = Files.createTempDirectory("coupling").absolutePathString()
