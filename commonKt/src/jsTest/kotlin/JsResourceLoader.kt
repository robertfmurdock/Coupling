import kotlin.js.Json

fun loadResource(name: String) : Json {
    return js("require(name)").unsafeCast<Json>()
}