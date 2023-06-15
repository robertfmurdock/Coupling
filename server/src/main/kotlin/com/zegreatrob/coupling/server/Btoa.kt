package com.zegreatrob.coupling.server

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@ExperimentalEncodingApi
fun btoa(s: String): String = Base64.encode(s.encodeToByteArray())
