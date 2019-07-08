fun loadStyles(@Suppress("UNUSED_PARAMETER") name: String): dynamic {
    return js("require('../../resources/main/com/zegreatrob/coupling/client/'+ name +'.css')")
}
