package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.express.env
import com.zegreatrob.coupling.server.external.express.Express
import com.zegreatrob.coupling.server.external.express.Handler
import com.zegreatrob.coupling.server.external.parse5htmlrewritingstream.RewritingStream
import com.zegreatrob.coupling.server.external.parse5htmlrewritingstream.Tag
import com.zegreatrob.coupling.server.external.stream.Readable
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.promise
import kotools.types.text.toNotBlankString
import web.http.fetch

val indexHtmlPromise
    get() = MainScope().promise { fetch("${Config.clientUrl}/html/index.html").textAsync().await() }

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
                replaceNextText = Config.appTitle
            }
            if (tag.tagName == "head") {
                rewritingStream.emitRaw(injectVariablesForClient())
            }
        }

        rewritingStream.on("text") { _, raw ->
            val text = replaceNextText
            if (text != null) {
                rewritingStream.emitRaw(text).also { replaceNextText = null }
            } else {
                rewritingStream.emitRaw(raw)
            }
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

fun healthRoute(): Handler = { request, response, _ ->
    request.setUser(
        UserDetails(
            UserId("HealthCheck".toNotBlankString().getOrThrow()),
            "-".toNotBlankString().getOrThrow(),
            emptySet(),
            null,
        ),
    )
    request.scope.launch {
        request.commandDispatcher()
    }.invokeOnCompletion { error ->
        response.sendStatus(if (error == null) 200 else 500)
            .also { error?.let { println("exception ${error.message}") } }
            .also { error?.printStackTrace() }
    }
}
