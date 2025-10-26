package com.zegreatrob.coupling.client.components

@JsModule("com/zegreatrob/coupling/client/ConnectSuccess.md")
private external val connectSuccessMd: String

@JsModule("com/zegreatrob/coupling/client/ContributionStart.md")
private external val contributionStartMd: String

@JsModule("com/zegreatrob/coupling/client/Boost.md")
private external val boostMd: String

@JsModule("com/zegreatrob/coupling/client/recent-info.md")
private external val recentInfoMd: String

@JsModule("com/zegreatrob/coupling/client/About.md")
private external val aboutMd: String

@JsModule("com/zegreatrob/coupling/client/InstallSuccess.md")
private external val installSuccessMd: String

fun loadMarkdown() {
    MarkdownContent.content = MarkdownContent(
        connectSuccessMd = connectSuccessMd,
        contributionStartMd = contributionStartMd,
        boostMd = boostMd,
        recentInfoMd = recentInfoMd,
        aboutMd = aboutMd,
        installSuccessMd = installSuccessMd,
    )
}
