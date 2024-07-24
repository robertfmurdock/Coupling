package com.zegreatrob.coupling.repository.secret

interface SecretRepository :
    SecretSave,
    SecretSaveUsed,
    SecretListGet,
    SecretDelete
