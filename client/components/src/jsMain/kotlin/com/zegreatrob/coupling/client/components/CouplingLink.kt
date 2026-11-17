package com.zegreatrob.coupling.client.components

import csstype.Properties
import emotion.css.ClassName
import emotion.css.cx
import react.FC
import react.PropsWithChildren
import react.PropsWithClassName
import react.router.dom.Link
import web.cssom.VerticalAlign

external interface CouplingLinkProps :
    PropsWithChildren,
    PropsWithClassName {
    var to: String
    var sizeRuleSet: Properties?
    var colorRuleSet: Properties?
    var ariaLabel: String?
}

val CouplingLink = FC<CouplingLinkProps> { props ->
    Link {
        to = props.to
        ariaLabel = props.ariaLabel
        draggable = false
        className = cx(
            ClassName {
                "*" { verticalAlign = VerticalAlign.middle }
                buttonRuleset()
                +(props.sizeRuleSet ?: medium)
                +(props.colorRuleSet ?: black)
            },
            props.className,
        )
        +props.children
    }
}
