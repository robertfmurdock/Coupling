fun <T> loadResource(name: String) : T {
    return js("require(name)").unsafeCast<T>()
}