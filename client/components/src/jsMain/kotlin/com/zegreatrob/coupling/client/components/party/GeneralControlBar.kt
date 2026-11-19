package com.zegreatrob.coupling.client.components.party

import com.zegreatrob.coupling.client.components.NotificationButton
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.PropsWithChildren
import react.ReactNode
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.span
import web.cssom.AlignItems
import web.cssom.Color
import web.cssom.Display
import web.cssom.FlexWrap
import web.cssom.FontWeight
import web.cssom.Margin
import web.cssom.Position
import web.cssom.TextAlign
import web.cssom.VerticalAlign
import web.cssom.em
import web.cssom.number
import web.cssom.px

external interface GeneralControlBarProps : PropsWithChildren {
    var title: String
    var splashComponent: ReactNode
    var showNotifications: Boolean?
}

@ReactFunc
val GeneralControlBar by nfc<GeneralControlBarProps> { props ->
    div {
        css {
            backgroundColor = Color("#faf0d2")
            borderRadius = 50.px
        }
        div {
            css {
                display = Display.inline
                textAlign = TextAlign.left
                margin = (5.px)
            }
            div {
                css {
                    // Preserve the former h1 layout while keeping controls outside its semantics.
                    fontSize = 2.em
                    fontWeight = FontWeight.bold
                    marginTop = 0.67.em
                    marginBottom = 0.67.em
                    position = Position.relative
                    flexGrow = number(2.0)
                    display = Display.inlineFlex
                    flexWrap = FlexWrap.wrap
                    alignItems = AlignItems.center
                }
                span {
                    css { display = Display.inlineFlex }
                    span {
                        css {
                            margin = (10.px)
                            display = Display.flex
                            alignItems = AlignItems.center
                        }
                        +props.splashComponent
                    }
                    div {
                        css {
                            display = Display.flex
                            alignItems = AlignItems.center
                            "*" {
                                verticalAlign = VerticalAlign.middle
                            }
                        }
                        h1 {
                            css {
                                fontSize = 1.em
                                margin = 0.px
                                flexGrow = number(2.0)
                                textAlign = TextAlign.left
                            }
                            +props.title
                        }
                    }
                }

                div {
                    css {
                        display = Display.flex
                        alignItems = AlignItems.center
                        "*" {
                            verticalAlign = VerticalAlign.middle
                        }
                    }
                    span {
                        css { margin = Margin(0.px, 20.px) }
                        +props.children
                    }
                }
                if (props.showNotifications == true) {
                    span {
                        css {
                            position = Position.absolute
                            right = (-60).px
                            bottom = 0.px
                        }
                        NotificationButton()
                    }
                }
            }
        }
    }
}
