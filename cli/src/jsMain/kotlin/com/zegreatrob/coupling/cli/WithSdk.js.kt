package com.zegreatrob.coupling.cli

import web.url.URL

actual fun getHost(url: String): String = URL(url).host
