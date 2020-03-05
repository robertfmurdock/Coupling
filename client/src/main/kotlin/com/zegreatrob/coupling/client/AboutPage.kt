package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.external.reactmarkdown.markdown
import com.zegreatrob.coupling.client.routing.PageProps
import kotlinx.css.div
import react.dom.div

object AboutPage : FRComponent<PageProps>(provider()) {

    val styles = useStyles("About")

    override fun render(props: PageProps) = reactElement {
        div(classes = styles.className) {
            div {
                markdown(loadMarkdown("About"))
            }
        }
    }
}