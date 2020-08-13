package com.zegreatrob.coupling.wdio

object By {
    fun className(className: String): String = ".$className"
    fun id(id: String): String = "#$id"
}
