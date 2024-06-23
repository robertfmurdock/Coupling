package com.zegreatrob.coupling.action

actual inline fun <reified T> loadResource(fileResource: String): T = kotlinext.js.require("./$fileResource")
