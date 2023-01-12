package com.zegreatrob.coupling.components.external.reactmarkdown

import com.zegreatrob.coupling.components.waitForAsyncReactComponent
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
