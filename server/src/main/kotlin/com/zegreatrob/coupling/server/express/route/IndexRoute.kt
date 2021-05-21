package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.express.env
import com.zegreatrob.coupling.server.external.express.Express
import com.zegreatrob.coupling.server.external.express.Handler
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.fs.fs
import com.zegreatrob.coupling.server.external.node_fetch.fetch
import com.zegreatrob.coupling.server.external.parse5htmlrewritingstream.RewritingStream
import com.zegreatrob.coupling.server.external.parse5htmlrewritingstream.Tag
import com.zegreatrob.coupling.server.external.stream.Readable
import kotlin.js.Promise

val indexHtmlPromise by lazy {
    if (Config.clientPath.startsWith("/"))
        fetchLocally(Config.clientPath)
    else
        fetchRemotely()
}

private fun fetchLocally(filePath: String): Promise<String> = Promise.resolve(
    Promise.resolve(fs.readFileSync("$filePath/index.html", "utf8"))
)

private fun fetchRemotely(): Promise<String> = fetch("${Config.clientPath}/index.html").then { it.text() }
    .unsafeCast<Promise<String>>()

fun Express.indexRoute(): Handler = { request, response, _ ->
    indexHtmlPromise.then { indexHtml ->
        val indexStream = Readable.from(arrayOf(indexHtml))

        val rewritingStream = RewritingStream()
        var replaceNextText: String? = null
        rewritingStream.on("startTag") { tag ->
            if (Config.clientPath.startsWith("http"))
                rewriteLinksToStaticResources(tag)

            rewritingStream.emitStartTag(tag)

            if (tag.tagName == "title") {
                replaceNextText = "Coupling"
            }
            if (tag.tagName == "head") {
                rewritingStream.emitRaw(injectVariablesForClient(request))
            }
        }

        rewritingStream.on("text") { _, raw ->
            val text = replaceNextText
            if (text != null)
                rewritingStream.emitRaw(text).also { replaceNextText = null }
            else
                rewritingStream.emitRaw(raw)
        }

        rewritingStream.on("endTag") { tag ->
            rewritingStream.emitEndTag(tag)
        }

        indexStream.pipe(rewritingStream).pipe(response)
    }
}

private fun rewriteLinksToStaticResources(tag: Tag) {
    tag.attrs = tag.attrs.map { attribute ->
        attribute.apply {
            this.value = this.value.replace("/app/build", Config.clientPath)
        }
    }.toTypedArray()
}

private fun Express.injectVariablesForClient(request: Request) = """<script>
    window.googleClientId = "${Config.googleClientID}";
    window.auth0ClientId = "${Config.AUTH0_CLIENT_ID}";
    window.auth0Domain = "${Config.AUTH0_DOMAIN}";
    window.expressEnv = "$env";
    window.isAuthenticated = ${request.isAuthenticated()};
    window.webpackPublicPath = "${Config.clientPath}/";
    </script>
""".trimIndent()