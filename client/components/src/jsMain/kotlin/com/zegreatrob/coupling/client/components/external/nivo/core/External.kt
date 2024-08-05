@file:JsModule("@nivo/core")

package com.zegreatrob.coupling.client.components.external.nivo.core

import react.Provider

external fun useTheme(): CompleteTheme

external val ThemeProvider: Provider<CompleteTheme>

external interface CompleteTheme
