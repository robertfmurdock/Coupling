import com.zegreatrob.coupling.server.buildApp

@Suppress("unused")
@JsExport
@JsName("serverless")
fun serverless(event: dynamic, context: dynamic): dynamic {
    event.path = if (event.path.unsafeCast<String?>() == "")  "/" else event.path
    return js("require('serverless-http')")(app)(event, context)
}

private val app by lazy {
    buildApp()
}
