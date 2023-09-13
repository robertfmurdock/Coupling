package com.zegreatrob.coupling.cli

import node.url.URL

actual fun getHost(url: String): String = URL(url).host
