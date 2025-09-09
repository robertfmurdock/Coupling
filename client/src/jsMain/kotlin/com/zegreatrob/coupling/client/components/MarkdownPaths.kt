package com.zegreatrob.coupling.client.components

@JsModule("com/zegreatrob/coupling/client/ConnectSuccess.md")
private external val connectSuccessMd: MarkdownModule

@JsModule("com/zegreatrob/coupling/client/ContributionStart.md")
private external val contributionStartMd: MarkdownModule

@JsModule("com/zegreatrob/coupling/client/Boost.md")
private external val boostMd: MarkdownModule

@JsModule("com/zegreatrob/coupling/client/recent-info.md")
private external val recentInfoMd: MarkdownModule

@JsModule("com/zegreatrob/coupling/client/About.md")
private external val aboutMd: MarkdownModule

@JsModule("com/zegreatrob/coupling/client/InstallSuccess.md")
private external val installSuccessMd: MarkdownModule

external interface MarkdownModule {
    val default: String
}

fun loadMarkdown() {
    MarkdownContent.content = MarkdownContent(
        connectSuccessMd = connectSuccessMd.default,
        contributionStartMd = contributionStartMd.default,
        boostMd = boostMd.default,
        recentInfoMd = recentInfoMd.default,
        aboutMd = aboutMd.default,
        installSuccessMd = installSuccessMd.default,
    )
}
