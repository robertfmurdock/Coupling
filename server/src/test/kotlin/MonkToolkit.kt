interface MonkToolkit {
    fun id(): String {
        @Suppress("UNUSED_VARIABLE")
        val monk = js("require(\"monk\")")
        return js("monk.id()").toString()
    }
}
