fun <T> loadStyles(@Suppress("UNUSED_PARAMETER") name: String): T {
    return js("require('../../resources/main/com/zegreatrob/coupling/client/'+ name +'.css')").unsafeCast<T>()
}
