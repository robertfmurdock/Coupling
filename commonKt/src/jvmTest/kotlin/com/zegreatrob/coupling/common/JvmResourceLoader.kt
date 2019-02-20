package com.zegreatrob.coupling.common

actual inline fun <reified T> loadResource(fileResource: String): T {
    return mapper.readValue(TribeSetup::class.java.classLoader.getResource(fileResource), T::class.java)
}