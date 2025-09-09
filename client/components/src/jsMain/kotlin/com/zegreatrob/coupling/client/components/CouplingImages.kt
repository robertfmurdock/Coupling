package com.zegreatrob.coupling.client.components

data class CouplingImages(
    val overlayPng: String,
    val graphqlSvg: String,
    val logoSvg: String,
    val noPartyImagePath: String,
    val noPlayerImagePath: String,
    val frodo: String,
    val samwise: String,
    val grayson: String,
    val wayne: String,
    val rosie: String,
    val wendy: String,
    val robPng: String,
    val autumnPng: String,
) {
    companion object {
        var images = CouplingImages(
            overlayPng = "overlayPng",
            graphqlSvg = "graphqlSvg",
            logoSvg = "logoSvg",
            noPartyImagePath = "noPartyImagePath",
            noPlayerImagePath = "noPlayerImagePath",
            frodo = "frodo",
            samwise = "samwise",
            grayson = "grayson",
            wayne = "wayne",
            rosie = "rosie",
            wendy = "wendy",
            robPng = "robPng",
            autumnPng = "autumnPng",
        )
    }
}
