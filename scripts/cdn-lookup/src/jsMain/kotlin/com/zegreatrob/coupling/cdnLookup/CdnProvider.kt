package com.zegreatrob.coupling.cdnLookup

internal interface CdnProvider {
    fun urlFor(request: CdnProviderRequest): String
}

internal const val DEFAULT_CDN_PROVIDER_NAME = "esm.sh"

private val cdnProviders = mapOf(DEFAULT_CDN_PROVIDER_NAME to EsmShCdnProvider())

internal fun selectedCdnProvider(name: String = DEFAULT_CDN_PROVIDER_NAME): CdnProvider = cdnProviders[name]
    ?: error("Unsupported CDN provider '$name'")

internal data class CdnProviderRequest(
    val importName: String,
    val version: String,
    val dependencies: List<CdnProviderDependency> = emptyList(),
    val external: List<String> = emptyList(),
)

internal data class CdnProviderDependency(
    val name: String,
    val version: String,
)

internal class EsmShCdnProvider : CdnProvider {

    override fun urlFor(request: CdnProviderRequest): String {
        val (module, submodule) = request.importName.moduleAndSubmodule()
        return "https://esm.sh/$module@${request.version}$submodule${request.queryParameters()}"
    }
}

private fun CdnProviderRequest.queryParameters(): String {
    val params = buildList {
        if (dependencies.isNotEmpty()) {
            val deps = dependencies.joinToString(",") { dependency -> "${dependency.name}@${dependency.version}" }
            add("deps=$deps")
        }
        if (external.isNotEmpty()) {
            val encodedExternal = external.joinToString(",") { encodeQueryParamValue(it) }
            add("external=$encodedExternal")
        }
    }
    return if (params.isEmpty()) "" else "?${params.joinToString("&")}"
}

private fun encodeQueryParamValue(value: String): String = js("encodeURIComponent")(value).unsafeCast<String>()

private fun String.moduleAndSubmodule(): Pair<String, String> {
    val split = indexOf("/")
    return if (startsWith("@") || split < 0) {
        this to ""
    } else {
        take(split) to substring(split)
    }
}
