package com.zegreatrob.coupling.cli

actual suspend fun writeSecureData(key: String, text: String) {
    ksafe.putEncrypted(key, text)
}
