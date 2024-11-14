package com.zegreatrob.coupling.action

actual inline fun <reified T> loadResource(fileResource: String): T = require("./$fileResource")

external fun <T> require(module: String): T
