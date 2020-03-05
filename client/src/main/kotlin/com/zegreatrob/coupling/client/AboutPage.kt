package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.FRComponent
import com.zegreatrob.coupling.client.external.react.loadMarkdown
import com.zegreatrob.coupling.client.external.react.provider
import com.zegreatrob.coupling.client.external.react.reactElement
import com.zegreatrob.coupling.client.external.reactmarkdown.markdown
import com.zegreatrob.coupling.client.routing.PageProps

object AboutPage : FRComponent<PageProps>(provider()) {
    override fun render(props: PageProps) = reactElement {
        markdown(loadMarkdown("About"))
    }
}