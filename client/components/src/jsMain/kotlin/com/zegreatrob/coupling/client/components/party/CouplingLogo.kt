package com.zegreatrob.coupling.client.components.party

import com.zegreatrob.coupling.client.components.svgPath
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.svg.ReactSVG
import react.dom.svg.ReactSVG.svg

external interface CouplingLogoProps : Props {
    var width: Double
    var height: Double
}

@ReactFunc
val CouplingLogo by nfc<CouplingLogoProps> { (width, height) ->
    svg {
        fill = "none"
        viewBox = "0 0 36 24"
        this.width = width
        this.height = height
        ReactSVG.image {
            this.href = svgPath("logo")
        }
    }
}
