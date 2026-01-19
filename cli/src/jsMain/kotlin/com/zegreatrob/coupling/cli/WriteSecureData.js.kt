package com.zegreatrob.coupling.cli

import com.zegreatrob.coupling.cli.external.napirskeyring.Entry

actual suspend fun writeSecureData(key: String, text: String) {
    val keyringEntry = Entry("coupling-cli", key)
    keyringEntry.setPassword(text)
}
