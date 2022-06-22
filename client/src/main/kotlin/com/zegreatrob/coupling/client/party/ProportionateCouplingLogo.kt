package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.components.svgPath
import react.FC
import react.Props
import react.dom.svg.ReactSVG

external interface CouplingLogoProps : Props {
    var width: Double
    var height: Double
}

val CouplingLogo = FC<CouplingLogoProps> { props ->
    ReactSVG.svg {
        fill = "none"
        viewBox = "0 0 36 24"
        this.width = props.width
        this.height = props.height
        ReactSVG.image {
            this.href = svgPath("logo")
        }
    }
}
