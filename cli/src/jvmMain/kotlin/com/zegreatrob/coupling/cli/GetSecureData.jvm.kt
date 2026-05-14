package com.zegreatrob.coupling.cli

actual suspend fun getSecureData(key: String): String? = ksafe.get(key, defaultValue = null)
