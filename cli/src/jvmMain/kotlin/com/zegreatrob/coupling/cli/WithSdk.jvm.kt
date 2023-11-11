package com.zegreatrob.coupling.cli

import java.net.URI

actual fun getHost(url: String): String = URI(url).host
