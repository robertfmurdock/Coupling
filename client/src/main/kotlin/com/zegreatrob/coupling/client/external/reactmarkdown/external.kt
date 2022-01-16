package com.zegreatrob.coupling.client.external.reactmarkdown

import com.zegreatrob.coupling.client.waitForAsyncReactComponent
import kotlinx.browser.window
import org.w3c.dom.get
import react.ElementType
import react.FC
import react.Props

val Markdown: ElementType<Props> = FC { props ->
    waitForAsyncReactComponent({ window["ReactMarkdown"].unsafeCast<ElementType<Props>?>() }) { component ->
        component { +props }
    }
}
