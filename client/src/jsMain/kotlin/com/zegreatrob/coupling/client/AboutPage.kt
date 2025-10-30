package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.components.MarkdownContent
import com.zegreatrob.coupling.client.components.external.marked.parse
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.minreact.nfc
import js.lazy.Lazy
import react.dom.DangerouslySetInnerHTML
import react.dom.html.ReactHTML.div
import web.html.HtmlSource

@Lazy
val AboutPage by nfc<PageProps> {
    aboutPageContent {
        div {
            dangerouslySetInnerHTML = DangerouslySetInnerHTML(
                __html = HtmlSource(parse(MarkdownContent.content.aboutMd)),
            )
        }
    }
}
