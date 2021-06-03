import com.zegreatrob.coupling.server.buildApp

@Suppress("unused")
@JsExport
@JsName("serverless")
fun serverless(event: dynamic, context: dynamic): dynamic = js("require('serverless-http')")(app)(event, context)

private val app by lazy {
    buildApp()
}
