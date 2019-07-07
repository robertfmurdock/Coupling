import react.RBuilder
import react.dom.img

val gravatarUrl: dynamic = js("require('../../../app/components/player-card/GravatarHelper').gravatarUrl")

external interface GravatarOptions {
    @Suppress("unused")
    val default: String
    val size: Int
}

fun RBuilder.gravatarImage(
        email: String?,
        fallback: String?,
        className: String?,
        alt: String?,
        options: GravatarOptions
) = img(
        src = myGravatarUrl(options, email, fallback),
        alt = alt
) {
    withAttributes(mapOf(
            "width" to options.size,
            "height" to options.size,
            "className" to className
    ))
}

private fun myGravatarUrl(options: GravatarOptions, email: String?, fallback: String?) =
        if (email == null && fallback != null) {
            fallback
        } else {
            gravatarUrl(email, options) as String
        }
