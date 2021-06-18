package com.zegreatrob.coupling.server.express.middleware

import com.zegreatrob.coupling.server.UserDataService
import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.external.express.Express
import com.zegreatrob.coupling.server.external.passport.passport
import com.zegreatrob.coupling.server.external.passportauth0.auth0Strategy

fun Express.passport() {
    use(passport.initialize())
    use(passport.session())

    passport.serializeUser(UserDataService::serializeUser)
    passport.deserializeUser(UserDataService::deserializeUser)

    passport.use(auth0Strategy())

    if (Config.TEST_LOGIN_ENABLED) {
        passport.use(localStrategy())
    }
}