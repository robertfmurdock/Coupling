package com.zegreatrob.coupling.server.express

import com.zegreatrob.coupling.server.UserDataService
import com.zegreatrob.coupling.server.external.express.Express
import com.zegreatrob.coupling.server.external.passport.passport

fun Express.passport() {
    use(passport.initialize())
    use(passport.session())

    passport.serializeUser(UserDataService::serializeUser)
    passport.deserializeUser(UserDataService::deserializeUser)

    passport.use(googleAuthTransferStrategy())
    passport.use(azureODICStrategy())

    if (isInDevMode) {
        passport.use(localStrategy())
    }
}