fun <T> loadResource(@Suppress("UNUSED_PARAMETER") name: String) : T {
    return js("require(name)").unsafeCast<T>()
}