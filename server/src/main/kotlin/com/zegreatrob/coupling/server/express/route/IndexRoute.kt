package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.express.env
import com.zegreatrob.coupling.server.express.middleware.resourcePath
import com.zegreatrob.coupling.server.external.express.Express
import com.zegreatrob.coupling.server.external.express.Handler
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.fs.fs
import com.zegreatrob.coupling.server.external.parse5htmlrewritingstream.RewritingStream
import com.zegreatrob.coupling.server.external.stream.Readable

val indexHtml = fs.readFileSync("${resourcePath("public")}/app/build/index.html", "utf8")

fun Express.indexRoute(): Handler = { request, response, _ ->
    val indexStream = Readable.from(arrayOf(indexHtml))

    val rewritingStream = RewritingStream()
    var replaceNextText: String? = null
    rewritingStream.on("startTag") { tag ->
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

private fun Express.injectVariablesForClient(request: Request) = """<script>
    window.googleClientId = "${Config.googleClientID}";
    window.expressEnv = "$env";
    window.isAuthenticated = ${request.isAuthenticated()}
    </script>
""".trimIndent()