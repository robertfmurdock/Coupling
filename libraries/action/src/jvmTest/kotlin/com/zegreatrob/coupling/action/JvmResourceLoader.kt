package com.zegreatrob.coupling.action

actual inline fun <reified T> loadResource(fileResource: String): T = mapper.readValue(PartySetup::class.java.classLoader.getResource(fileResource), T::class.java)
