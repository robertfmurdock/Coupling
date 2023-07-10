package com.zegreatrob.coupling.client.components.external.reactmarkdown

import com.zegreatrob.coupling.client.components.waitForAsyncReactComponent
import kotlinx.browser.window
import org.w3c.dom.get
import react.ElementType
import react.FC
import react.Props
import react.create

val Markdown: ElementType<Props> = FC { props ->
    waitForAsyncReactComponent({ window["ReactMarkdown"].unsafeCast<ElementType<Props>?>() }) { component ->
        component.create { +props }
    }
}
