import com.zegreatrob.coupling.server.buildApp

//@Suppress("unused")
//@JsExport
//@JsName("handler")
//val handler = { event: dynamic, context: dynamic ->
//    println("Here's the function that should be used by serverless")
//    js("require('serverless-http')")(buildApp())(event, context)
//}.also {
//    println("handler is $it")
//}
//

private val app by lazy {
    buildApp()
}

@Suppress("unused")
@JsExport
@JsName("serverless")
fun serverless(event: dynamic, context: dynamic) {
    js("require('serverless-http')")(app)(event, context)
}
