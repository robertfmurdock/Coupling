package com.zegreatrob.coupling.actionFunc

interface MasterDispatchSyntax : ExecutableActionDispatcherSyntax, SuspendActionDispatcherExecuteSyntax {
    override val dispatcher: MasterDispatcher
}
