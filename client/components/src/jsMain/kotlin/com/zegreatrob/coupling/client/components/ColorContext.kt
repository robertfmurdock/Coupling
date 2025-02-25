package com.zegreatrob.coupling.client.components

import react.createContext

val colorContext = createContext<(dynamic) -> String> { "" }
