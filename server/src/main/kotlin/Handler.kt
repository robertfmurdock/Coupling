import com.zegreatrob.coupling.server.buildApp
import com.zegreatrob.coupling.server.external.express.Express

@Suppress("unused")
@JsExport
@JsName("handler")
fun handler(): dynamic {
    println("Here's the function that should be used by serverless")
    val app = buildApp()
    return js("require('serverless-http')")(app)
}
