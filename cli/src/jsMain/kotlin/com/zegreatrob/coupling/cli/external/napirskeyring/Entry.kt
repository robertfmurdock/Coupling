@file:JsModule("@napi-rs/keyring")

package com.zegreatrob.coupling.cli.external.napirskeyring

external class Entry(service: String, name: String) {
    fun getPassword(): String?
    fun setPassword(password: String)
    fun deletePassword()
}
