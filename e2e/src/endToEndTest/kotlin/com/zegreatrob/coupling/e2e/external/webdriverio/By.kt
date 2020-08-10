package com.zegreatrob.coupling.e2e.external.webdriverio

object By {
    fun className(className: String): String = ".$className"
    fun id(id: String): String = "#$id"
}
