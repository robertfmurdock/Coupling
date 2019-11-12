package com.zegreatrob.coupling.server.entity.tribe

interface TribeDispatcherJs : SaveTribeCommandDispatcherJs,
    TribeListQueryDispatcherJs,
    TribeQueryDispatcherJs,
    DeleteTribeCommandDispatcherJs {
}