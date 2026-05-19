package com.zegreatrob.coupling.plugins.util

import java.io.File

fun File.parsePlanEnv(): Map<String, String> = readLines()
    .filter { it.contains("=") }
    .associate { line ->
        val i = line.indexOf('=')
        line.substring(0, i) to line.substring(i + 1).trim('\'')
    }
