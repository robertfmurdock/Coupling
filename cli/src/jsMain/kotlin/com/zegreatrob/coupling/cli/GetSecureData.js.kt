package com.zegreatrob.coupling.cli

import com.zegreatrob.coupling.cli.external.napirskeyring.Entry

actual suspend fun getSecureData(key: String): String? {
    val keyringEntry = Entry("coupling-cli", key)
    return keyringEntry.getPassword()
}
