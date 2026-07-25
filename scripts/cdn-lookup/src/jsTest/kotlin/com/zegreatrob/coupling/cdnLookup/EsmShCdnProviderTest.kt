package com.zegreatrob.coupling.cdnLookup

import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test

class EsmShCdnProviderTest {

    @Test
    fun buildsBaseUrlForPackageVersionAndSubpath() {
        EsmShCdnProvider()
            .urlFor(CdnProviderRequest(importName = "fixture-package/subpath", version = "1.2.3"))
            .assertIsEqualTo("https://esm.sh/fixture-package@1.2.3/subpath")
    }

    @Test
    fun buildsDependencyQueryWithPinnedVersions() {
        EsmShCdnProvider()
            .urlFor(
                CdnProviderRequest(
                    importName = "fixture-package",
                    version = "1.2.3",
                    dependencies = listOf(
                        CdnProviderDependency("react", "19.2.8"),
                        CdnProviderDependency("react-dom", "19.2.8"),
                    ),
                ),
            )
            .assertIsEqualTo("https://esm.sh/fixture-package@1.2.3?deps=react@19.2.8,react-dom@19.2.8")
    }

    @Test
    fun buildsExternalQueryWithEncodedImports() {
        EsmShCdnProvider()
            .urlFor(
                CdnProviderRequest(
                    importName = "fixture-package",
                    version = "1.2.3",
                    external = listOf("react/jsx-runtime", "@remix-run/router"),
                ),
            )
            .assertIsEqualTo("https://esm.sh/fixture-package@1.2.3?external=react%2Fjsx-runtime,%40remix-run%2Frouter")
    }
}
