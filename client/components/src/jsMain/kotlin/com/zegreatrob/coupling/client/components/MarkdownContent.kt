package com.zegreatrob.coupling.client.components

data class MarkdownContent(
    val aboutMd: String,
    val connectSuccessMd: String,
    val contributionStartMd: String,
    val boostMd: String,
    val recentInfoMd: String,
    val installSuccessMd: String,
) {
    companion object {
        var content = MarkdownContent(
            aboutMd = "aboutMd",
            boostMd = "boostMd",
            connectSuccessMd = "connectSuccessMd",
            contributionStartMd = "contributionStartMd",
            installSuccessMd = "installSuccessMd",
            recentInfoMd = "recentInfoMd",
        )
    }
}
