import com.zegreatgrob.coupling.cdnLookup.generateCdnRef
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement

fun main() {
    val libs = processArguments().toList()
    MainScope().launch {
        generateCdnRef(libs)
            .toJson()
            .let { println(it) }
    }
}

private fun List<Pair<String, String>>.toJson() = Json.encodeToJsonElement(toMap())

private fun processArguments() = js("process.argv.splice(2)").unsafeCast<Array<String>>()
