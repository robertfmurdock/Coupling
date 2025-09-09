package com.zegreatrob.coupling.client.components

@JsModule("images/overlay.png")
private external val overlayPng: String

@JsModule("images/graphql.svg")
private external val graphqlSvg: String

@JsModule("images/logo.svg")
private external val logoSvg: String

@JsModule("images/parties/no-party.png")
private external val noPartyImagePath: String

@JsModule("images/players/autumn.png")
private external val noPlayerImagePath: String

@JsModule("images/players/frodo.png")
private external val frodo: String

@JsModule("images/players/samwise.png")
private external val samwise: String

@JsModule("images/players/grayson.png")
private external val grayson: String

@JsModule("images/players/wayne.png")
private external val wayne: String

@JsModule("images/players/rosie.png")
private external val rosie: String

@JsModule("images/players/wendy.png")
private external val wendy: String

@JsModule("images/players/rob.png")
private external val robPng: String

@JsModule("images/players/autumn.png")
private external val autumnPng: String

fun loadImages() {
    CouplingImages.images = CouplingImages(
        overlayPng = overlayPng,
        graphqlSvg = graphqlSvg,
        logoSvg = logoSvg,
        noPartyImagePath = noPartyImagePath,
        noPlayerImagePath = noPlayerImagePath,
        frodo = frodo,
        samwise = samwise,
        grayson = grayson,
        wayne = wayne,
        rosie = rosie,
        wendy = wendy,
        robPng = robPng,
        autumnPng = autumnPng,
    )
}
