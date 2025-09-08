package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.i
import web.cssom.ClassName
import web.cssom.em
import web.window.WindowName

external interface ContributionLinkButtonProps : Props {
    var link: String
}

@ReactFunc
val ContributionLinkButton by nfc<ContributionLinkButtonProps> { (link) ->
    div {
        css { margin = 0.6.em }
        a {
            href = link
            target = WindowName("Contribution Link")
            i { className = ClassName("fa fa-arrow-up-right-from-square") }
        }
    }
}
