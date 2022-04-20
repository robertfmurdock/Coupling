package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.express.env
import com.zegreatrob.coupling.server.external.express.Express
import com.zegreatrob.coupling.server.external.express.Handler
import com.zegreatrob.coupling.server.external.node_fetch.FetchResult
import com.zegreatrob.coupling.server.external.node_fetch.fetch
import com.zegreatrob.coupling.server.external.parse5htmlrewritingstream.RewritingStream
import com.zegreatrob.coupling.server.external.parse5htmlrewritingstream.Tag
import com.zegreatrob.coupling.server.external.stream.Readable
import kotlin.js.Promise

val indexHtmlPromise get() = fetch("${Config.clientUrl}/html/index.html")
    .then(FetchResult::text)
    .unsafeCast<Promise<String>>()

fun Express.indexRoute(): Handler = { _, response, _ ->
    indexHtmlPromise.then { indexHtml ->
        response.type("html")
        val indexStream = Readable.from(arrayOf(indexHtml))

        val rewritingStream = RewritingStream()
        var replaceNextText: String? = null
        rewritingStream.on("startTag") { tag ->

            rewriteLinksToStaticResources(tag)

            rewritingStream.emitStartTag(tag)

            if (tag.tagName == "title") {
                replaceNextText = "Coupling"
            }
            if (tag.tagName == "head") {
                rewritingStream.emitRaw(injectVariablesForClient())
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
            this.value = this.value.replace("/app/build", Config.clientUrl)
        }
    }.toTypedArray()
}

private fun Express.injectVariablesForClient() = """<script>
    window.prereleaseMode = ${Config.prereleaseMode};
    window.auth0ClientId = "${Config.AUTH0_CLIENT_ID}";
    window.auth0Domain = "${Config.AUTH0_DOMAIN}";
    window.basename = "${Config.clientBasename}";
    window.expressEnv = "$env";
    window.webpackPublicPath = "${Config.clientUrl}/";
    window.websocketHost = "${Config.websocketHost}/";
    </script>
""".trimIndent()
