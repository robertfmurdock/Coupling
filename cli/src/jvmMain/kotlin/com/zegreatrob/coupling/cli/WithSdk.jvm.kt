package com.zegreatrob.coupling.cli

import java.net.URL

actual fun getHost(url: String): String = URL(url).host
